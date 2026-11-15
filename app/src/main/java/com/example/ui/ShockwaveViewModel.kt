package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.ExportBackupManager
import com.example.data.export.ShockwaveBackupPayload
import com.example.data.local.ShockwaveDatabase
import com.example.data.local.TournamentEntity
import com.example.data.local.WalletTransactionEntity
import com.example.data.model.DateFilter
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import com.example.data.model.OutcomeFilter
import com.example.data.model.ReportPeriod
import com.example.data.model.WalletTxType
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AllTimeStats
import com.example.data.repository.ChartPoint
import com.example.data.repository.CommandAlert
import com.example.data.repository.DashboardSummary
import com.example.data.repository.PeriodReportRow
import com.example.data.repository.ShockwaveAnalytics
import com.example.data.repository.ShockwaveRepository
import com.example.data.repository.WalletSummary
import com.example.i18n.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HistoryFilterState(
    val searchQuery: String = "",
    val dateFilter: DateFilter = DateFilter.ALL_TIME,
    val modeFilter: GameMode? = null,
    val outcomeFilter: OutcomeFilter = OutcomeFilter.ALL
)

class ShockwaveViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ShockwaveDatabase.getInstance(application)
    private val prefsRepo = UserPreferencesRepository(application)
    private val repository = ShockwaveRepository(database.shockwaveDao(), prefsRepo)

    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val allTournaments: StateFlow<List<TournamentEntity>> = repository.tournamentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = repository.walletTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dashboardDateFilter = MutableStateFlow(DateFilter.THIS_MONTH)
    val dashboardDateFilter: StateFlow<DateFilter> = _dashboardDateFilter.asStateFlow()

    private val _chartDateFilter = MutableStateFlow(DateFilter.THIS_MONTH)
    val chartDateFilter: StateFlow<DateFilter> = _chartDateFilter.asStateFlow()

    private val _historyFilter = MutableStateFlow(HistoryFilterState())
    val historyFilter: StateFlow<HistoryFilterState> = _historyFilter.asStateFlow()

    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.DAILY)
    val selectedReportPeriod: StateFlow<ReportPeriod> = _selectedReportPeriod.asStateFlow()

    private val _isPinUnlocked = MutableStateFlow(false)
    val isPinUnlocked: StateFlow<Boolean> = _isPinUnlocked.asStateFlow()

    private val _dismissedAlertIds = MutableStateFlow<Set<String>>(emptySet())

    private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiMessages: SharedFlow<String> = _uiMessages.asSharedFlow()

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        allTournaments,
        _dashboardDateFilter
    ) { list, filter ->
        val filtered = ShockwaveAnalytics.filterByDate(list, filter)
        ShockwaveAnalytics.computeDashboardSummary(filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    val chartPoints: StateFlow<List<ChartPoint>> = combine(
        allTournaments,
        _chartDateFilter
    ) { list, filter ->
        ShockwaveAnalytics.buildChartPoints(list, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletSummary: StateFlow<WalletSummary> = combine(
        preferences,
        walletTransactions
    ) { prefs, txs ->
        ShockwaveAnalytics.computeWalletSummary(prefs.startingBalance, txs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletSummary())

    val commandAlerts: StateFlow<List<CommandAlert>> = combine(
        allTournaments,
        walletSummary,
        _dismissedAlertIds
    ) { list, wallet, dismissed ->
        ShockwaveAnalytics.deriveCommandAlerts(list, wallet)
            .filterNot { it.id in dismissed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHistory: StateFlow<List<TournamentEntity>> = combine(
        allTournaments,
        _historyFilter
    ) { list, filter ->
        val byDate = ShockwaveAnalytics.filterByDate(list, filter.dateFilter)
        byDate.filter { item ->
            val matchesQuery = filter.searchQuery.isBlank() ||
                item.name.contains(filter.searchQuery, ignoreCase = true) ||
                item.notes.contains(filter.searchQuery, ignoreCase = true)
            val matchesMode = filter.modeFilter == null || item.gameMode == filter.modeFilter
            val matchesOutcome = when (filter.outcomeFilter) {
                OutcomeFilter.ALL -> true
                OutcomeFilter.PROFIT -> item.isProfit
                OutcomeFilter.LOSS -> item.isLoss
                OutcomeFilter.BREAK_EVEN -> item.isBreakEven
            }
            matchesQuery && matchesMode && matchesOutcome
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportRows: StateFlow<List<PeriodReportRow>> = combine(
        allTournaments,
        _selectedReportPeriod
    ) { list, period ->
        ShockwaveAnalytics.computePeriodReports(list, period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimeStats: StateFlow<AllTimeStats> = allTournaments
        .combine(_selectedReportPeriod) { list, _ ->
            ShockwaveAnalytics.computeAllTimeStats(list)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AllTimeStats())

    fun setDashboardDateFilter(filter: DateFilter) {
        _dashboardDateFilter.value = filter
        _chartDateFilter.value = filter
    }

    fun setChartDateFilter(filter: DateFilter) {
        _chartDateFilter.value = filter
    }

    fun updateHistorySearch(query: String) {
        _historyFilter.value = _historyFilter.value.copy(searchQuery = query)
    }

    fun updateHistoryDateFilter(filter: DateFilter) {
        _historyFilter.value = _historyFilter.value.copy(dateFilter = filter)
    }

    fun updateHistoryModeFilter(mode: GameMode?) {
        _historyFilter.value = _historyFilter.value.copy(modeFilter = mode)
    }

    fun updateHistoryOutcomeFilter(outcome: OutcomeFilter) {
        _historyFilter.value = _historyFilter.value.copy(outcomeFilter = outcome)
    }

    fun setReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
    }

    fun dismissAlert(id: String) {
        _dismissedAlertIds.value = _dismissedAlertIds.value + id
    }

    fun clearAllAlerts() {
        _dismissedAlertIds.value = _dismissedAlertIds.value + commandAlerts.value.map { it.id }
    }

    suspend fun getTournamentById(id: Long): TournamentEntity? = repository.getTournamentById(id)

    fun saveTournament(
        id: Long = 0L,
        name: String,
        dateMillis: Long,
        gameMode: GameMode,
        entryFee: Double,
        totalWinning: Double,
        additionalExpenses: Double,
        matchResult: MatchResult,
        notes: String,
        syncWithWallet: Boolean,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = TournamentEntity(
                id = id,
                name = name.trim(),
                dateMillis = dateMillis,
                gameMode = gameMode,
                entryFee = entryFee.coerceAtLeast(0.0),
                totalWinning = totalWinning.coerceAtLeast(0.0),
                additionalExpenses = additionalExpenses.coerceAtLeast(0.0),
                matchResult = matchResult,
                notes = notes.trim(),
                syncWithWallet = syncWithWallet
            )
            repository.saveTournament(entity)
            onDone()
        }
    }

    fun deleteTournament(id: Long) {
        viewModelScope.launch {
            repository.deleteTournament(id)
        }
    }

    fun addManualWalletTransaction(type: WalletTxType, amount: Double, description: String) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addManualWalletTransaction(
                type = type,
                amount = amount,
                description = description.ifBlank {
                    if (type == WalletTxType.DEPOSIT) "Team Deposit" else "Team Withdrawal"
                }
            )
        }
    }

    fun deleteWalletTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteWalletTransaction(id)
        }
    }

    fun updateStartingBalance(amount: Double) {
        viewModelScope.launch {
            repository.setStartingBalance(amount.coerceAtLeast(0.0))
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.setLanguage(language)
        }
    }

    fun setTeamIdentity(name: String, tagline: String) {
        viewModelScope.launch {
            repository.setTeamIdentity(name, tagline)
        }
    }

    fun enablePin(rawPin: String) {
        viewModelScope.launch {
            repository.setPin(rawPin)
            _isPinUnlocked.value = true
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            repository.disablePin()
            _isPinUnlocked.value = true
        }
    }

    fun verifyPin(rawPin: String): Boolean {
        val ok = repository.verifyPin(rawPin, preferences.value.pinHash)
        if (ok) {
            _isPinUnlocked.value = true
        }
        return ok
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.loadSampleTeamData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun exportCsvToUri(uri: Uri, successMsg: String, errorMsg: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val tournaments = repository.getAllTournamentsOnce()
                    val txs = repository.getAllWalletTransactionsOnce()
                    val wallet = ShockwaveAnalytics.computeWalletSummary(
                        preferences.value.startingBalance,
                        txs
                    )
                    val csv = ExportBackupManager.generateCsv(tournaments, wallet)
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(csv.toByteArray(Charsets.UTF_8))
                    } ?: error("Null output stream")
                }
            }
            _uiMessages.emit(if (result.isSuccess) successMsg else errorMsg)
        }
    }

    fun exportPdfToUri(uri: Uri, periodLabel: String, successMsg: String, errorMsg: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val tournaments = repository.getAllTournamentsOnce()
                    val txs = repository.getAllWalletTransactionsOnce()
                    val summary = ShockwaveAnalytics.computeDashboardSummary(tournaments)
                    val wallet = ShockwaveAnalytics.computeWalletSummary(
                        preferences.value.startingBalance,
                        txs
                    )
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        ExportBackupManager.writePdfReport(
                            outputStream = out,
                            teamName = preferences.value.teamName,
                            periodLabel = periodLabel,
                            tournaments = tournaments,
                            summary = summary,
                            walletSummary = wallet
                        )
                    } ?: error("Null output stream")
                }
            }
            _uiMessages.emit(if (result.isSuccess) successMsg else errorMsg)
        }
    }

    fun exportJsonBackupToUri(uri: Uri, successMsg: String, errorMsg: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val tournaments = repository.getAllTournamentsOnce()
                    val txs = repository.getAllWalletTransactionsOnce()
                    val prefs = preferences.value
                    val payload = ExportBackupManager.exportJsonBackup(
                        tournaments = tournaments,
                        transactions = txs,
                        startingBalance = prefs.startingBalance,
                        teamName = prefs.teamName,
                        teamTagline = prefs.teamTagline
                    )
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(payload.toByteArray(Charsets.UTF_8))
                    } ?: error("Null output stream")
                }
            }
            _uiMessages.emit(if (result.isSuccess) successMsg else errorMsg)
        }
    }

    suspend fun parseBackupFromUri(uri: Uri): Result<ShockwaveBackupPayload> =
        withContext(Dispatchers.IO) {
            runCatching {
                val raw = getApplication<Application>().contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: error("Unable to read file")
                ExportBackupManager.parseJsonBackup(raw)
            }
        }

    fun applyBackupPayload(
        payload: ShockwaveBackupPayload,
        replaceExisting: Boolean,
        successMsg: String
    ) {
        viewModelScope.launch {
            repository.restoreBackup(
                tournaments = payload.tournaments,
                transactions = payload.walletTransactions,
                startingBalance = payload.startingBalance,
                teamName = payload.teamName,
                teamTagline = payload.teamTagline,
                replaceExisting = replaceExisting
            )
            _uiMessages.emit(successMsg)
        }
    }
}
