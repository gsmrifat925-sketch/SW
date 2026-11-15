package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DateFilter
import com.example.data.preferences.UserPreferences
import com.example.data.repository.ChartPoint
import com.example.data.repository.CommandAlert
import com.example.i18n.LocalStrings
import com.example.ui.components.FinancialMetricCard
import com.example.ui.components.NeonFilterPill
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.ProfitLossLineChart
import com.example.ui.components.ShockwaveSLightningLogo
import com.example.ui.components.ShockwaveTeamAvatar
import com.example.ui.components.ShockwaveWordmark
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.DeepCrimsonCard
import com.example.ui.theme.DeepEmeraldCard
import com.example.ui.theme.DeepPurpleCard
import com.example.ui.theme.DeepTealCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TealGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    preferences: UserPreferences,
    totalInvestment: Double,
    totalWinnings: Double,
    totalProfit: Double,
    totalLoss: Double,
    totalMatches: Int,
    winningMatches: Int,
    losingMatches: Int,
    totalTournamentCount: Int,
    selectedDateFilter: DateFilter,
    chartDateFilter: DateFilter,
    chartPoints: List<ChartPoint>,
    alerts: List<CommandAlert>,
    onSelectDateFilter: (DateFilter) -> Unit,
    onSelectChartFilter: (DateFilter) -> Unit,
    onAddTournamentClick: () -> Unit,
    onOpenWalletClick: () -> Unit,
    onOpenReportsClick: () -> Unit,
    onOpenHistoryClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onDismissAlert: (String) -> Unit,
    onClearAllAlerts: () -> Unit,
    onLoadSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var showAlertsSheet by remember { mutableStateOf(false) }
    var chartMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // 1. Futuristic Hero Banner Header with Logo, Title, Notifications & Settings
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(198.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "SHOCKWAVE Esports Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Smooth fade into pure black at left and bottom
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PureBlack.copy(alpha = 0.25f),
                                        Color.Transparent,
                                        PureBlack
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        PureBlack.copy(alpha = 0.88f),
                                        PureBlack.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            ShockwaveSLightningLogo(size = 54.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            ShockwaveWordmark(fontSize = 23.sp)
                            Text(
                                text = strings.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                letterSpacing = 1.4.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PureBlack.copy(alpha = 0.65f))
                                    .border(1.dp, NeonPurple.copy(alpha = 0.55f), CircleShape)
                                    .clickable { showAlertsSheet = true }
                                    .minimumInteractiveComponentSize()
                                    .testTag("home_notifications_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = strings.notificationsTitle,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(21.dp)
                                )
                                if (alerts.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(NeonRed)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PureBlack.copy(alpha = 0.65f))
                                    .border(1.dp, NeonPurple.copy(alpha = 0.55f), CircleShape)
                                    .clickable(onClick = onOpenSettingsClick)
                                    .minimumInteractiveComponentSize()
                                    .testTag("home_settings_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = strings.navSettings,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Team Identity Pill Card (SHOCKWAVE • Play • Win • Grow >)
            item {
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("team_identity_card"),
                    glowColor = NeonPurple,
                    containerBrush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1F0C3D), Color(0xFF120826), Color(0xFF1A0A35))
                    ),
                    cornerRadius = 20.dp,
                    onClick = onOpenWalletClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShockwaveTeamAvatar(size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preferences.teamName,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = OrbitronFontFamily,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = preferences.teamTagline,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = BrightViolet
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. Date Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateFilter.entries.forEach { filter ->
                        val label = when (filter) {
                            DateFilter.TODAY -> strings.filterToday
                            DateFilter.SEVEN_DAYS -> strings.filter7Days
                            DateFilter.THIS_MONTH -> strings.filterThisMonth
                            DateFilter.ALL_TIME -> strings.filterAllTime
                        }
                        val icon = if (filter == DateFilter.TODAY || filter == DateFilter.SEVEN_DAYS) {
                            Icons.Default.CalendarToday
                        } else null
                        NeonFilterPill(
                            label = label,
                            selected = selectedDateFilter == filter,
                            icon = icon,
                            testTag = "filter_${filter.name.lowercase()}",
                            onClick = { onSelectDateFilter(filter) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Empty State Onboarding Banner (only when DB has 0 matches)
            if (totalTournamentCount == 0) {
                item {
                    NeonGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        glowColor = ElectricBlue
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.emptyDashboardTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.emptyDashboardSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onLoadSampleData,
                                modifier = Modifier.testTag("load_sample_data_btn")
                            ) {
                                Text(strings.loadSampleDataBtn, color = ElectricBlue)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // 5. Four Glowing Financial Cards (2x2 Grid)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FinancialMetricCard(
                            title = strings.totalInvestment,
                            amount = totalInvestment,
                            icon = Icons.Default.AccountBalanceWallet,
                            accentColor = BrightViolet,
                            secondaryGlow = BrightViolet,
                            cardGradientColors = listOf(DeepPurpleCard, Color(0xFF120729)),
                            testTag = "card_total_investment",
                            onClick = onOpenReportsClick,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            title = strings.totalWinning,
                            amount = totalWinnings,
                            icon = Icons.Default.EmojiEvents,
                            accentColor = TealGreen,
                            secondaryGlow = NeonGreen,
                            cardGradientColors = listOf(DeepEmeraldCard, Color(0xFF041C16)),
                            testTag = "card_total_winning",
                            onClick = onOpenReportsClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FinancialMetricCard(
                            title = strings.totalProfit,
                            amount = totalProfit,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            accentColor = NeonGreen,
                            secondaryGlow = ElectricBlue,
                            cardGradientColors = listOf(DeepTealCard, Color(0xFF04171D)),
                            testTag = "card_total_profit",
                            onClick = onOpenReportsClick,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            title = strings.totalLoss,
                            amount = totalLoss,
                            icon = Icons.Default.HeartBroken,
                            accentColor = NeonRed,
                            secondaryGlow = NeonRed.copy(alpha = 0.9f),
                            cardGradientColors = listOf(DeepCrimsonCard, Color(0xFF17050B)),
                            useChevronArrow = true,
                            testTag = "card_total_loss",
                            onClick = onOpenHistoryClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 6. Match Count Row (Total Matches / Wins / Losses)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MatchCounterCard(
                        icon = Icons.Default.EmojiEvents,
                        iconTint = GoldCrown,
                        value = totalMatches.toString(),
                        label = strings.totalMatches,
                        testTag = "stat_total_matches",
                        onClick = onOpenHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                    MatchCounterCard(
                        icon = Icons.Default.WorkspacePremium,
                        iconTint = GoldCrown,
                        value = winningMatches.toString(),
                        label = strings.winningMatches,
                        testTag = "stat_winning_matches",
                        onClick = onOpenHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                    MatchCounterCard(
                        icon = Icons.Default.GpsFixed,
                        iconTint = ElectricBlue,
                        value = losingMatches.toString(),
                        label = strings.losingMatches,
                        testTag = "stat_losing_matches",
                        onClick = onOpenHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 7. Profit vs Loss Glowing Line Chart Card
            item {
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("profit_loss_chart_card"),
                    glowColor = NeonPurple,
                    cornerRadius = 22.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Equalizer,
                                    contentDescription = null,
                                    tint = BrightViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.profitVsLoss,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }

                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { chartMenuExpanded = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val periodLabel = when (chartDateFilter) {
                                        DateFilter.TODAY -> strings.filterToday
                                        DateFilter.SEVEN_DAYS -> strings.filter7Days
                                        DateFilter.THIS_MONTH -> strings.filterThisMonth
                                        DateFilter.ALL_TIME -> strings.filterAllTime
                                    }
                                    Text(
                                        text = periodLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = chartMenuExpanded,
                                    onDismissRequest = { chartMenuExpanded = false },
                                    modifier = Modifier.background(CardDarkElevated)
                                ) {
                                    DateFilter.entries.forEach { f ->
                                        val lbl = when (f) {
                                            DateFilter.TODAY -> strings.filterToday
                                            DateFilter.SEVEN_DAYS -> strings.filter7Days
                                            DateFilter.THIS_MONTH -> strings.filterThisMonth
                                            DateFilter.ALL_TIME -> strings.filterAllTime
                                        }
                                        DropdownMenuItem(
                                            text = { Text(lbl, color = TextPrimary) },
                                            onClick = {
                                                chartMenuExpanded = false
                                                onSelectChartFilter(f)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.profitLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.lossLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        ProfitLossLineChart(points = chartPoints)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 8. Large Glowing "+ ADD TOURNAMENT" CTA Button
            item {
                NeonPrimaryButton(
                    text = strings.addTournamentBtn,
                    onClick = onAddTournamentClick,
                    testTag = "home_add_tournament_btn",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showAlertsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAlertsSheet = false },
            containerColor = CardDarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.notificationsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    if (alerts.isNotEmpty()) {
                        TextButton(onClick = onClearAllAlerts) {
                            Text("Clear", color = BrightViolet)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (alerts.isEmpty()) {
                    Text(
                        text = strings.noNotifications,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    alerts.forEach { alert ->
                        NeonGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            glowColor = if (alert.isWarning) NeonRed else NeonGreen,
                            cornerRadius = 14.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (alert.isWarning) NeonRed else NeonGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = alert.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(onClick = { onDismissAlert(alert.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MatchCounterCard(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeonGlassCard(
        modifier = modifier
            .testTag(testTag)
            .minimumInteractiveComponentSize(),
        glowColor = NeonPurple.copy(alpha = 0.55f),
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
