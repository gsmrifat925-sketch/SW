package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ShockwaveDatabase
import com.example.data.local.TournamentEntity
import com.example.data.model.DateFilter
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ShockwaveRepository
import com.example.ui.HomeDashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeDashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var db: ShockwaveDatabase
    private lateinit var repository: ShockwaveRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShockwaveDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShockwaveRepository(db.shockwaveDao(), UserPreferencesRepository(context))
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `home dashboard viewmodel aggregates investment winnings profit and loss stateflows`() = runBlocking {
        val fixedNow = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 14, 14, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val viewModel = HomeDashboardViewModel(repository, clockMillis = { fixedNow })

        // Keep all downstream StateFlows subscribed
        val collectJob = launch(testDispatcher) {
            launch { viewModel.totalInvestment.collect {} }
            launch { viewModel.totalWinnings.collect {} }
            launch { viewModel.totalProfit.collect {} }
            launch { viewModel.totalLoss.collect {} }
            launch { viewModel.totalMatches.collect {} }
        }

        // Insert a profitable match today (Entry 800 + Extra 200 = 1000 Invest, Winning 2500 -> Profit +1500)
        repository.saveTournament(
            TournamentEntity(
                name = "Today Win",
                dateMillis = fixedNow - 3600_000L,
                gameMode = GameMode.SQUAD,
                entryFee = 800.0,
                additionalExpenses = 200.0,
                totalWinning = 2500.0,
                matchResult = MatchResult.WIN
            )
        )
        // Insert a losing match 3 days ago (Entry 500 + Extra 100 = 600 Invest, Winning 200 -> Loss 400)
        repository.saveTournament(
            TournamentEntity(
                name = "3D Loss",
                dateMillis = fixedNow - 3L * 24L * 3600_000L,
                gameMode = GameMode.DUO,
                entryFee = 500.0,
                additionalExpenses = 100.0,
                totalWinning = 200.0,
                matchResult = MatchResult.LOSS
            )
        )
        // Insert an older match from last month (Entry 400, Winning 900 -> Profit +500)
        repository.saveTournament(
            TournamentEntity(
                name = "Last Month Win",
                dateMillis = fixedNow - 35L * 24L * 3600_000L,
                gameMode = GameMode.SOLO,
                entryFee = 400.0,
                additionalExpenses = 0.0,
                totalWinning = 900.0,
                matchResult = MatchResult.WIN
            )
        )

        // Wait for Room's async invalidation tracker to push THIS_MONTH (2 matches)
        withTimeout(5000) {
            viewModel.totalMatches.first { it == 2 }
            viewModel.totalInvestment.first { it == 1600.0 }
        }

        assertEquals(1600.0, viewModel.totalInvestment.value, 0.01)
        assertEquals(2700.0, viewModel.totalWinnings.value, 0.01)
        assertEquals(1500.0, viewModel.totalProfit.value, 0.01)
        assertEquals(400.0, viewModel.totalLoss.value, 0.01)

        // Switch filter to TODAY -> only Today Win
        viewModel.setDateFilter(DateFilter.TODAY)
        withTimeout(5000) {
            viewModel.totalMatches.first { it == 1 }
            viewModel.totalInvestment.first { it == 1000.0 }
        }
        assertEquals(1000.0, viewModel.totalInvestment.value, 0.01)
        assertEquals(2500.0, viewModel.totalWinnings.value, 0.01)
        assertEquals(1500.0, viewModel.totalProfit.value, 0.01)
        assertEquals(0.0, viewModel.totalLoss.value, 0.01)

        // Switch filter to ALL_TIME -> all 3 matches
        viewModel.setDateFilter(DateFilter.ALL_TIME)
        withTimeout(5000) {
            viewModel.totalMatches.first { it == 3 }
            viewModel.totalInvestment.first { it == 2000.0 }
        }
        assertEquals(2000.0, viewModel.totalInvestment.value, 0.01)
        assertEquals(3600.0, viewModel.totalWinnings.value, 0.01)
        assertEquals(2000.0, viewModel.totalProfit.value, 0.01)
        assertEquals(400.0, viewModel.totalLoss.value, 0.01)

        collectJob.cancel()
    }
}
