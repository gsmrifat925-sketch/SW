package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ShockwaveDatabase
import com.example.data.local.TournamentEntity
import com.example.data.local.WalletTransactionEntity
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import com.example.data.model.WalletTxType
import com.example.data.repository.ShockwaveAnalytics
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: ShockwaveDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShockwaveDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SHOCKWAVE", appName)
    }

    @Test
    fun `tournament edit and delete never duplicate wallet transactions`() = runTest {
        val dao = db.shockwaveDao()
        val now = System.currentTimeMillis()

        // Insert initial SQUAD tournament: Entry 800, Extra 200, Winning 2500
        val id = dao.saveTournamentWithWalletSync(
            TournamentEntity(
                name = "FF Scrim #1",
                dateMillis = now,
                gameMode = GameMode.SQUAD,
                entryFee = 800.0,
                totalWinning = 2500.0,
                additionalExpenses = 200.0,
                matchResult = MatchResult.WIN,
                syncWithWallet = true
            )
        )

        var txs = dao.getAllWalletTransactionsOnce()
        assertEquals(3, txs.size)

        // Update the same tournament: Entry 900, Extra 100, Winning 3000
        dao.saveTournamentWithWalletSync(
            TournamentEntity(
                id = id,
                name = "FF Scrim #1 Updated",
                dateMillis = now,
                gameMode = GameMode.SQUAD,
                entryFee = 900.0,
                totalWinning = 3000.0,
                additionalExpenses = 100.0,
                matchResult = MatchResult.WIN,
                syncWithWallet = true
            )
        )

        txs = dao.getAllWalletTransactionsOnce()
        assertEquals(3, txs.size)
        val wallet = ShockwaveAnalytics.computeWalletSummary(startingBalance = 5000.0, transactions = txs)
        // 5000 + 3000 - 900 - 100 = 7000
        assertEquals(7000.0, wallet.availableBalance, 0.01)

        // Add manual deposit & withdrawal
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                type = WalletTxType.DEPOSIT,
                amount = 500.0,
                description = "Sponsor",
                dateMillis = now
            )
        )
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                type = WalletTxType.WITHDRAWAL,
                amount = 1000.0,
                description = "Team split",
                dateMillis = now
            )
        )

        // Delete tournament and verify linked wallet entries are reverted while manual txs stay
        dao.deleteTournamentWithWalletSync(id)
        val remaining = dao.getAllWalletTransactionsOnce()
        assertEquals(2, remaining.size)
        assertTrue(remaining.all { it.tournamentId == null })
        val finalWallet = ShockwaveAnalytics.computeWalletSummary(5000.0, remaining)
        // 5000 + 500 - 1000 = 4500
        assertEquals(4500.0, finalWallet.availableBalance, 0.01)
    }
}
