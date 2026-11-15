package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportPeriod
import com.example.data.repository.AllTimeStats
import com.example.data.repository.PeriodReportRow
import com.example.i18n.LocalStrings
import com.example.i18n.formatTaka
import com.example.ui.components.NeonFilterPill
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.DeepPurpleCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ReportsScreen(
    selectedPeriod: ReportPeriod,
    reportRows: List<PeriodReportRow>,
    allTimeStats: AllTimeStats,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .statusBarsPadding()
        ) {
            // Header + Export actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.reportsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = OrbitronFontFamily,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportChipButton(
                        label = "PDF",
                        icon = Icons.Default.PictureAsPdf,
                        accent = NeonPurple,
                        testTag = "reports_export_pdf_btn",
                        onClick = onExportPdf
                    )
                    ExportChipButton(
                        label = "CSV",
                        icon = Icons.Default.TableChart,
                        accent = ElectricBlue,
                        testTag = "reports_export_csv_btn",
                        onClick = onExportCsv
                    )
                }
            }

            // Period Tabs: Daily, Weekly, Monthly, Yearly, All-Time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.entries.forEach { period ->
                    val label = when (period) {
                        ReportPeriod.DAILY -> strings.periodDaily
                        ReportPeriod.WEEKLY -> strings.periodWeekly
                        ReportPeriod.MONTHLY -> strings.periodMonthly
                        ReportPeriod.YEARLY -> strings.periodYearly
                        ReportPeriod.ALL_TIME -> strings.periodAllTime
                    }
                    NeonFilterPill(
                        label = label,
                        selected = selectedPeriod == period,
                        testTag = "report_tab_${period.name.lowercase()}",
                        onClick = { onSelectPeriod(period) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (allTimeStats.summary.totalMatches == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noReportsData,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else if (selectedPeriod == ReportPeriod.ALL_TIME) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AllTimeHeroCard(stats = allTimeStats)
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatBoxCard(
                                title = strings.winRateLabel,
                                value = String.format(Locale.ENGLISH, "%.1f%%", allTimeStats.winRatePercent),
                                accent = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatBoxCard(
                                title = strings.roiLabel,
                                value = String.format(Locale.ENGLISH, "%+.1f%%", allTimeStats.roiPercent),
                                accent = if (allTimeStats.roiPercent >= 0) ElectricBlue else NeonRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatBoxCard(
                                title = strings.avgInvestmentLabel,
                                value = formatTaka(allTimeStats.avgInvestment),
                                accent = BrightViolet,
                                modifier = Modifier.weight(1f)
                            )
                            StatBoxCard(
                                title = strings.avgWinningLabel,
                                value = formatTaka(allTimeStats.avgWinning),
                                accent = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    allTimeStats.bestTournament?.let { best ->
                        item {
                            NeonGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                glowColor = GoldCrown,
                                cornerRadius = 18.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = strings.bestTournamentLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GoldCrown
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = best.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = formatTaka(best.netResult, showSign = true),
                                            fontFamily = OrbitronFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            text = strings.modeBreakdownTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(allTimeStats.modeBreakdowns, key = { it.mode.name }) { modeRow ->
                        val netColor = if (modeRow.netPnL >= 0) NeonGreen else NeonRed
                        NeonGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = NeonPurple.copy(alpha = 0.55f),
                            cornerRadius = 16.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = modeRow.mode.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = BrightViolet
                                    )
                                    Text(
                                        text = "${modeRow.matchCount} ${strings.matchesCount} • ${modeRow.wins} ${strings.winningMatches}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatTaka(modeRow.netPnL, showSign = true),
                                        fontFamily = OrbitronFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = netColor
                                    )
                                    Text(
                                        text = "${strings.totalWinning}: ${formatTaka(modeRow.winning)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(18.dp)) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportRows, key = { it.periodKey }) { row ->
                        PeriodReportCard(row = row)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportChipButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
    }
}

@Composable
private fun PeriodReportCard(row: PeriodReportRow) {
    val strings = LocalStrings.current
    val netColor = when {
        row.netPnL > 0.005 -> NeonGreen
        row.netPnL < -0.005 -> NeonRed
        else -> ElectricBlue
    }
    val totalBarBase = (row.totalInvestment + row.totalWinning).coerceAtLeast(1.0)
    val winningWeight = (row.totalWinning / totalBarBase).toFloat().coerceIn(0.05f, 0.95f)

    NeonGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("report_row_${row.periodKey}"),
        glowColor = netColor.copy(alpha = 0.65f),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = row.periodTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${row.matchCount} ${strings.matchesCount}  •  ${row.winCount}W / ${row.lossCount}L",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = strings.netPnLLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = formatTaka(row.netPnL, showSign = true),
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = netColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Winning vs Investment Neon Ratio Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CardDarkElevated)
            ) {
                Box(
                    modifier = Modifier
                        .weight(winningWeight)
                        .fillMaxSize()
                        .background(NeonGreen)
                )
                Box(
                    modifier = Modifier
                        .weight(1f - winningWeight)
                        .fillMaxSize()
                        .background(NeonPurple)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReportStatCol(strings.totalInvestment, formatTaka(row.totalInvestment), BrightViolet)
                ReportStatCol(strings.totalWinning, formatTaka(row.totalWinning), ElectricBlue)
                ReportStatCol(strings.profitLabel, formatTaka(row.totalProfit), NeonGreen)
                ReportStatCol(strings.lossLabel, formatTaka(row.totalLoss), NeonRed)
            }
        }
    }
}

@Composable
private fun ReportStatCol(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = color
        )
    }
}

@Composable
private fun AllTimeHeroCard(stats: AllTimeStats) {
    val strings = LocalStrings.current
    val s = stats.summary
    val netColor = if (s.netPnL >= 0) NeonGreen else NeonRed

    NeonGlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = NeonPurple,
        containerBrush = Brush.linearGradient(listOf(DeepPurpleCard, Color(0xFF0C071A))),
        cornerRadius = 20.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = strings.periodAllTime.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = BrightViolet
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTaka(s.netPnL, showSign = true),
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = netColor
            )
            Text(
                text = "${strings.netPnLLabel}  •  ${s.totalMatches} ${strings.matchesCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReportStatCol(strings.totalInvestment, formatTaka(s.totalInvestment), BrightViolet)
                ReportStatCol(strings.totalWinning, formatTaka(s.totalWinning), ElectricBlue)
                ReportStatCol(strings.totalProfit, formatTaka(s.totalProfit), NeonGreen)
                ReportStatCol(strings.totalLoss, formatTaka(s.totalLoss), NeonRed)
            }
        }
    }
}

@Composable
private fun StatBoxCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    NeonGlassCard(
        modifier = modifier,
        glowColor = accent.copy(alpha = 0.65f),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = accent
            )
        }
    }
}
