package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.export.ShockwaveBackupPayload
import com.example.data.local.TournamentEntity
import com.example.i18n.LocalStrings
import com.example.i18n.stringsFor
import com.example.ui.components.PinLockScreen
import com.example.ui.components.ShockwaveSplashScreen
import com.example.ui.navigation.ShockwaveRoute
import com.example.ui.screens.AddEditTournamentScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class NavTabItem(
    val route: ShockwaveRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun ShockwaveApp(viewModel: ShockwaveViewModel) {
    val app = LocalContext.current.applicationContext as Application
    val homeDashboardViewModel: HomeDashboardViewModel = viewModel(
        factory = HomeDashboardViewModel.factory(app)
    )

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val isPinUnlocked by viewModel.isPinUnlocked.collectAsStateWithLifecycle()
    val allTournaments by viewModel.allTournaments.collectAsStateWithLifecycle()
    val dashboardDateFilter by homeDashboardViewModel.selectedDateFilter.collectAsStateWithLifecycle()
    val chartDateFilter by homeDashboardViewModel.chartDateFilter.collectAsStateWithLifecycle()
    val totalInvestment by homeDashboardViewModel.totalInvestment.collectAsStateWithLifecycle()
    val totalWinnings by homeDashboardViewModel.totalWinnings.collectAsStateWithLifecycle()
    val totalProfit by homeDashboardViewModel.totalProfit.collectAsStateWithLifecycle()
    val totalLoss by homeDashboardViewModel.totalLoss.collectAsStateWithLifecycle()
    val totalMatches by homeDashboardViewModel.totalMatches.collectAsStateWithLifecycle()
    val winningMatches by homeDashboardViewModel.winningMatches.collectAsStateWithLifecycle()
    val losingMatches by homeDashboardViewModel.losingMatches.collectAsStateWithLifecycle()
    val chartPoints by homeDashboardViewModel.chartPoints.collectAsStateWithLifecycle()
    val alerts by homeDashboardViewModel.commandAlerts.collectAsStateWithLifecycle()
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredHistory.collectAsStateWithLifecycle()
    val selectedReportPeriod by viewModel.selectedReportPeriod.collectAsStateWithLifecycle()
    val reportRows by viewModel.reportRows.collectAsStateWithLifecycle()
    val allTimeStats by viewModel.allTimeStats.collectAsStateWithLifecycle()
    val walletSummary by viewModel.walletSummary.collectAsStateWithLifecycle()
    val walletTransactions by viewModel.walletTransactions.collectAsStateWithLifecycle()

    val strings = stringsFor(preferences.language)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSplash by remember { mutableStateOf(true) }
    var pinError by remember { mutableStateOf<String?>(null) }
    var pendingRestorePayload by remember { mutableStateOf<ShockwaveBackupPayload?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // SAF Launchers for PDF, CSV, JSON Backup & JSON Restore
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.exportPdfToUri(
                uri = uri,
                periodLabel = selectedReportPeriod.name,
                successMsg = strings.exportSuccessMsg,
                errorMsg = strings.exportErrorMsg
            )
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportCsvToUri(
                uri = uri,
                successMsg = strings.exportSuccessMsg,
                errorMsg = strings.exportErrorMsg
            )
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportJsonBackupToUri(
                uri = uri,
                successMsg = strings.exportSuccessMsg,
                errorMsg = strings.exportErrorMsg
            )
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val parsed = viewModel.parseBackupFromUri(uri)
                if (parsed.isSuccess) {
                    pendingRestorePayload = parsed.getOrNull()
                } else {
                    snackbarHostState.showSnackbar(strings.restoreErrorMsg)
                }
            }
        }
    }

    fun fileStamp(): String = SimpleDateFormat("yyyyMMdd_HHmm", Locale.ENGLISH).format(Date())

    CompositionLocalProvider(LocalStrings provides strings) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
        ) {
            if (showSplash) {
                ShockwaveSplashScreen(onFinished = { showSplash = false })
            } else if (preferences.pinEnabled && !isPinUnlocked) {
                PinLockScreen(
                    title = strings.enterPinTitle,
                    subtitle = strings.unlockSubtitle,
                    errorMessage = pinError,
                    onPinComplete = { code ->
                        val ok = viewModel.verifyPin(code)
                        pinError = if (ok) null else strings.wrongPinError
                    }
                )
            } else {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                val showBottomNav = currentDestination?.hasRoute<ShockwaveRoute.AddEditTournament>() != true

                val navTabs = listOf(
                    NavTabItem(
                        route = ShockwaveRoute.Home,
                        label = strings.navHome,
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        testTag = "nav_home"
                    ),
                    NavTabItem(
                        route = ShockwaveRoute.Reports,
                        label = strings.navReports,
                        selectedIcon = Icons.Filled.Equalizer,
                        unselectedIcon = Icons.Outlined.Equalizer,
                        testTag = "nav_reports"
                    ),
                    NavTabItem(
                        route = ShockwaveRoute.History,
                        label = strings.navHistory,
                        selectedIcon = Icons.Filled.SportsEsports,
                        unselectedIcon = Icons.Outlined.SportsEsports,
                        testTag = "nav_history"
                    ),
                    NavTabItem(
                        route = ShockwaveRoute.Wallet,
                        label = strings.navWallet,
                        selectedIcon = Icons.Filled.AccountBalanceWallet,
                        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
                        testTag = "nav_wallet"
                    ),
                    NavTabItem(
                        route = ShockwaveRoute.Settings,
                        label = strings.navSettings,
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        testTag = "nav_settings"
                    )
                )

                fun navigateTab(target: ShockwaveRoute) {
                    navController.navigate(target) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useSideRail = maxWidth >= 600.dp

                    Row(modifier = Modifier.fillMaxSize()) {
                        if (useSideRail && showBottomNav) {
                            NavigationRail(
                                containerColor = DeepObsidian,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                navTabs.forEach { tab ->
                                    val selected = currentDestination?.hasRoute(tab.route::class) == true
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = { Text(tab.label) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = TextPrimary,
                                            selectedTextColor = BrightViolet,
                                            indicatorColor = NeonPurple.copy(alpha = 0.28f),
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted
                                        ),
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }

                        Scaffold(
                            containerColor = PureBlack,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                AnimatedVisibility(
                                    visible = showBottomNav && !useSideRail,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    ShockwaveBottomNavBar(
                                        tabs = navTabs,
                                        isSelected = { route ->
                                            currentDestination?.hasRoute(route::class) == true
                                        },
                                        onSelect = ::navigateTab
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = ShockwaveRoute.Home,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                composable<ShockwaveRoute.Home> {
                                    HomeDashboardScreen(
                                        preferences = preferences,
                                        totalInvestment = totalInvestment,
                                        totalWinnings = totalWinnings,
                                        totalProfit = totalProfit,
                                        totalLoss = totalLoss,
                                        totalMatches = totalMatches,
                                        winningMatches = winningMatches,
                                        losingMatches = losingMatches,
                                        totalTournamentCount = allTournaments.size,
                                        selectedDateFilter = dashboardDateFilter,
                                        chartDateFilter = chartDateFilter,
                                        chartPoints = chartPoints,
                                        alerts = alerts,
                                        onSelectDateFilter = homeDashboardViewModel::setDateFilter,
                                        onSelectChartFilter = homeDashboardViewModel::setChartDateFilter,
                                        onAddTournamentClick = {
                                            navController.navigate(ShockwaveRoute.AddEditTournament(0L))
                                        },
                                        onOpenWalletClick = { navigateTab(ShockwaveRoute.Wallet) },
                                        onOpenReportsClick = { navigateTab(ShockwaveRoute.Reports) },
                                        onOpenHistoryClick = { navigateTab(ShockwaveRoute.History) },
                                        onOpenSettingsClick = { navigateTab(ShockwaveRoute.Settings) },
                                        onDismissAlert = homeDashboardViewModel::dismissAlert,
                                        onClearAllAlerts = homeDashboardViewModel::clearAllAlerts,
                                        onLoadSampleData = viewModel::loadSampleData
                                    )
                                }

                                composable<ShockwaveRoute.Reports> {
                                    ReportsScreen(
                                        selectedPeriod = selectedReportPeriod,
                                        reportRows = reportRows,
                                        allTimeStats = allTimeStats,
                                        onSelectPeriod = viewModel::setReportPeriod,
                                        onExportPdf = {
                                            pdfLauncher.launch("SHOCKWAVE_Report_${fileStamp()}.pdf")
                                        },
                                        onExportCsv = {
                                            csvLauncher.launch("SHOCKWAVE_Tournaments_${fileStamp()}.csv")
                                        }
                                    )
                                }

                                composable<ShockwaveRoute.History> {
                                    HistoryScreen(
                                        tournaments = filteredHistory,
                                        filterState = historyFilter,
                                        language = preferences.language,
                                        onSearchChange = viewModel::updateHistorySearch,
                                        onDateFilterChange = viewModel::updateHistoryDateFilter,
                                        onModeFilterChange = viewModel::updateHistoryModeFilter,
                                        onOutcomeFilterChange = viewModel::updateHistoryOutcomeFilter,
                                        onAddClick = {
                                            navController.navigate(ShockwaveRoute.AddEditTournament(0L))
                                        },
                                        onEditClick = { tid ->
                                            navController.navigate(ShockwaveRoute.AddEditTournament(tid))
                                        },
                                        onDeleteConfirm = viewModel::deleteTournament
                                    )
                                }

                                composable<ShockwaveRoute.Wallet> {
                                    WalletScreen(
                                        walletSummary = walletSummary,
                                        transactions = walletTransactions,
                                        language = preferences.language,
                                        onAddManualTx = viewModel::addManualWalletTransaction,
                                        onUpdateStartingBalance = viewModel::updateStartingBalance,
                                        onDeleteManualTx = viewModel::deleteWalletTransaction
                                    )
                                }

                                composable<ShockwaveRoute.Settings> {
                                    SettingsScreen(
                                        preferences = preferences,
                                        onSaveTeamIdentity = viewModel::setTeamIdentity,
                                        onSelectLanguage = viewModel::setLanguage,
                                        onEnablePin = viewModel::enablePin,
                                        onDisablePin = viewModel::disablePin,
                                        onExportPdf = {
                                            pdfLauncher.launch("SHOCKWAVE_Report_${fileStamp()}.pdf")
                                        },
                                        onExportCsv = {
                                            csvLauncher.launch("SHOCKWAVE_Tournaments_${fileStamp()}.csv")
                                        },
                                        onBackupJson = {
                                            backupLauncher.launch("SHOCKWAVE_Backup_${fileStamp()}.json")
                                        },
                                        onRestoreJson = {
                                            restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                        },
                                        onLoadSampleData = viewModel::loadSampleData,
                                        onClearAllData = viewModel::clearAllData
                                    )
                                }

                                composable<ShockwaveRoute.AddEditTournament> { entry ->
                                    val route = entry.toRoute<ShockwaveRoute.AddEditTournament>()
                                    val existing by produceState<TournamentEntity?>(
                                        initialValue = null,
                                        key1 = route.tournamentId
                                    ) {
                                        value = if (route.tournamentId > 0L) {
                                            viewModel.getTournamentById(route.tournamentId)
                                        } else null
                                    }

                                    // Wait until loaded if editing so initial field state populates cleanly
                                    if (route.tournamentId == 0L || existing != null) {
                                        AddEditTournamentScreen(
                                            existingTournament = existing,
                                            language = preferences.language,
                                            onBack = { navController.popBackStack() },
                                            onSave = { id, name, date, mode, fee, win, exp, res, notes, sync ->
                                                viewModel.saveTournament(
                                                    id = id,
                                                    name = name,
                                                    dateMillis = date,
                                                    gameMode = mode,
                                                    entryFee = fee,
                                                    totalWinning = win,
                                                    additionalExpenses = exp,
                                                    matchResult = res,
                                                    notes = notes,
                                                    syncWithWallet = sync,
                                                    onDone = { navController.popBackStack() }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        pendingRestorePayload?.let { payload ->
            AlertDialog(
                onDismissRequest = { pendingRestorePayload = null },
                containerColor = CardDarkElevated,
                title = { Text(strings.restoreModeTitle, color = TextPrimary) },
                text = {
                    Text(
                        "${payload.tournaments.size} tournaments • ${payload.walletTransactions.size} wallet entries",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.applyBackupPayload(
                                payload = payload,
                                replaceExisting = true,
                                successMsg = strings.restoreSuccessMsg
                            )
                            pendingRestorePayload = null
                        }
                    ) {
                        Text(strings.restoreReplaceBtn, color = NeonGreen, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                viewModel.applyBackupPayload(
                                    payload = payload,
                                    replaceExisting = false,
                                    successMsg = strings.restoreSuccessMsg
                                )
                                pendingRestorePayload = null
                            }
                        ) {
                            Text(strings.restoreMergeBtn, color = BrightViolet)
                        }
                        TextButton(onClick = { pendingRestorePayload = null }) {
                            Text(strings.cancel, color = TextMuted)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ShockwaveBottomNavBar(
    tabs: List<NavTabItem>,
    isSelected: (ShockwaveRoute) -> Boolean,
    onSelect: (ShockwaveRoute) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xF00B0617), Color(0xFF05030A))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        NeonPurple.copy(alpha = 0.25f),
                        BrightViolet.copy(alpha = 0.55f),
                        NeonPurple.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = isSelected(tab.route)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (selected) {
                                Modifier
                                    .shadow(
                                        elevation = 10.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = NeonPurple,
                                        spotColor = NeonPurple
                                    )
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                ElectricViolet.copy(alpha = 0.48f),
                                                CardDarkSurface.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        BrightViolet.copy(alpha = 0.65f),
                                        RoundedCornerShape(16.dp)
                                    )
                            } else Modifier
                        )
                        .clickable { onSelect(tab.route) }
                        .minimumInteractiveComponentSize()
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                        .testTag(tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = if (selected) TextPrimary else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) TextPrimary else TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
