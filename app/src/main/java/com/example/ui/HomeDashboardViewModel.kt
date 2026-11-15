package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ShockwaveDatabase
import com.example.data.local.TournamentEntity
import com.example.data.model.DateFilter
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ChartPoint
import com.example.data.repository.CommandAlert
import com.example.data.repository.DashboardSummary
import com.example.data.repository.ShockwaveAnalytics
import com.example.data.repository.ShockwaveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface HomeDashboardUiState {
    data object Loading : HomeDashboardUiState
    data class Success(
        val dateFilter: DateFilter,
        val totalInvestment: Double,
        val totalWinnings: Double,
        val totalProfit: Double,
        val totalLoss: Double,
        val netPnL: Double,
        val totalMatches: Int,
        val winningMatches: Int,
        val losingMatches: Int,
        val totalTournamentCount: Int
    ) : HomeDashboardUiState
}

/**
 * ViewModel dedicated to the Home Dashboard.
 * Reactively aggregates tournament records from [ShockwaveRepository] to calculate
 * total investment, winnings, profit, and loss, exposing each metric as a StateFlow.
 */
class HomeDashboardViewModel(
    private val repository: ShockwaveRepository,
    private val clockMillis: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _selectedDateFilter = MutableStateFlow(DateFilter.THIS_MONTH)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter.asStateFlow()

    private val _chartDateFilter = MutableStateFlow(DateFilter.THIS_MONTH)
    val chartDateFilter: StateFlow<DateFilter> = _chartDateFilter.asStateFlow()

    private val _dismissedAlertIds = MutableStateFlow<Set<String>>(emptySet())

    private val filteredTournaments: StateFlow<List<TournamentEntity>> = combine(
        repository.tournamentsFlow,
        _selectedDateFilter
    ) { tournaments, filter ->
        ShockwaveAnalytics.filterByDate(tournaments, filter, clockMillis())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardSummary: StateFlow<DashboardSummary> = filteredTournaments
        .map { list -> ShockwaveAnalytics.computeDashboardSummary(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    val totalInvestment: StateFlow<Double> = dashboardSummary
        .map { it.totalInvestment }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalWinnings: StateFlow<Double> = dashboardSummary
        .map { it.totalWinning }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalProfit: StateFlow<Double> = dashboardSummary
        .map { it.totalProfit }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLoss: StateFlow<Double> = dashboardSummary
        .map { it.totalLoss }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netPnL: StateFlow<Double> = dashboardSummary
        .map { it.netPnL }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalMatches: StateFlow<Int> = dashboardSummary
        .map { it.totalMatches }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val winningMatches: StateFlow<Int> = dashboardSummary
        .map { it.winningMatches }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val losingMatches: StateFlow<Int> = dashboardSummary
        .map { it.losingMatches }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val chartPoints: StateFlow<List<ChartPoint>> = combine(
        repository.tournamentsFlow,
        _chartDateFilter
    ) { tournaments, filter ->
        ShockwaveAnalytics.buildChartPoints(tournaments, filter, clockMillis())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commandAlerts: StateFlow<List<CommandAlert>> = combine(
        repository.tournamentsFlow,
        repository.preferencesFlow,
        repository.walletTransactionsFlow,
        _dismissedAlertIds
    ) { tournaments, prefs, txs, dismissed ->
        val wallet = ShockwaveAnalytics.computeWalletSummary(prefs.startingBalance, txs)
        ShockwaveAnalytics.deriveCommandAlerts(tournaments, wallet)
            .filterNot { it.id in dismissed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<HomeDashboardUiState> = combine(
        repository.tournamentsFlow,
        dashboardSummary,
        _selectedDateFilter
    ) { allTournaments, summary, filter ->
        HomeDashboardUiState.Success(
            dateFilter = filter,
            totalInvestment = summary.totalInvestment,
            totalWinnings = summary.totalWinning,
            totalProfit = summary.totalProfit,
            totalLoss = summary.totalLoss,
            netPnL = summary.netPnL,
            totalMatches = summary.totalMatches,
            winningMatches = summary.winningMatches,
            losingMatches = summary.losingMatches,
            totalTournamentCount = allTournaments.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeDashboardUiState.Loading)

    fun setDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
        _chartDateFilter.value = filter
    }

    fun setChartDateFilter(filter: DateFilter) {
        _chartDateFilter.value = filter
    }

    fun dismissAlert(id: String) {
        _dismissedAlertIds.value = _dismissedAlertIds.value + id
    }

    fun clearAllAlerts() {
        _dismissedAlertIds.value = _dismissedAlertIds.value + commandAlerts.value.map { it.id }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = ShockwaveDatabase.getInstance(application).shockwaveDao()
                    val prefsRepo = UserPreferencesRepository(application)
                    val repository = ShockwaveRepository(dao, prefsRepo)
                    return HomeDashboardViewModel(repository) as T
                }
            }
    }
}
