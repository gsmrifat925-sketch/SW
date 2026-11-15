package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TournamentEntity
import com.example.data.model.DateFilter
import com.example.data.model.GameMode
import com.example.data.model.OutcomeFilter
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.i18n.formatDateDayHeader
import com.example.i18n.formatTaka
import com.example.ui.HistoryFilterState
import com.example.ui.components.NeonFilterPill
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    tournaments: List<TournamentEntity>,
    filterState: HistoryFilterState,
    language: AppLanguage,
    onSearchChange: (String) -> Unit,
    onDateFilterChange: (DateFilter) -> Unit,
    onModeFilterChange: (GameMode?) -> Unit,
    onOutcomeFilterChange: (OutcomeFilter) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var pendingDelete by remember { mutableStateOf<TournamentEntity?>(null) }

    val groupedByDay = remember(tournaments) {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        tournaments.groupBy { fmt.format(Date(it.dateMillis)) }
    }

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.historyTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = OrbitronFontFamily,
                    color = TextPrimary
                )
                Text(
                    text = "${tournaments.size} ${strings.matchesCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrightViolet
                )
            }

            // Search Bar
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(strings.searchHint, color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = BrightViolet)
                },
                trailingIcon = {
                    if (filterState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrightViolet,
                    unfocusedBorderColor = NeonPurple.copy(alpha = 0.35f),
                    focusedContainerColor = CardDarkElevated,
                    unfocusedContainerColor = CardDarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("history_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Date & Mode & Outcome Filter Rows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilter.entries.forEach { f ->
                    val lbl = when (f) {
                        DateFilter.TODAY -> strings.filterToday
                        DateFilter.SEVEN_DAYS -> strings.filter7Days
                        DateFilter.THIS_MONTH -> strings.filterThisMonth
                        DateFilter.ALL_TIME -> strings.filterAllTime
                    }
                    NeonFilterPill(
                        label = lbl,
                        selected = filterState.dateFilter == f,
                        testTag = "history_date_${f.name.lowercase()}",
                        onClick = { onDateFilterChange(f) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeonFilterPill(
                    label = strings.allModes,
                    selected = filterState.modeFilter == null,
                    testTag = "history_mode_all",
                    onClick = { onModeFilterChange(null) }
                )
                GameMode.entries.forEach { mode ->
                    val lbl = when (mode) {
                        GameMode.SOLO -> strings.modeSolo
                        GameMode.DUO -> strings.modeDuo
                        GameMode.SQUAD -> strings.modeSquad
                        GameMode.CUSTOM -> strings.modeCustom
                    }
                    NeonFilterPill(
                        label = lbl,
                        selected = filterState.modeFilter == mode,
                        testTag = "history_mode_${mode.name.lowercase()}",
                        onClick = { onModeFilterChange(mode) }
                    )
                }
                OutcomeFilter.entries.filter { it != OutcomeFilter.ALL }.forEach { outcome ->
                    val lbl = when (outcome) {
                        OutcomeFilter.PROFIT -> strings.profitLabel
                        OutcomeFilter.LOSS -> strings.lossLabel
                        OutcomeFilter.BREAK_EVEN -> strings.breakEvenLabel
                        OutcomeFilter.ALL -> strings.allOutcomes
                    }
                    NeonFilterPill(
                        label = lbl,
                        selected = filterState.outcomeFilter == outcome,
                        testTag = "history_outcome_${outcome.name.lowercase()}",
                        onClick = {
                            onOutcomeFilterChange(
                                if (filterState.outcomeFilter == outcome) OutcomeFilter.ALL else outcome
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (tournaments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.noHistoryTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.noHistorySubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groupedByDay.forEach { (_, dayMatches) ->
                        val dayMillis = dayMatches.first().dateMillis
                        val dayNet = dayMatches.sumOf { it.netResult }
                        item(key = "header_$dayMillis") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDateDayHeader(dayMillis, language),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextSecondary
                                )
                                Text(
                                    text = formatTaka(dayNet, showSign = true),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (dayNet > 0.005) NeonGreen else if (dayNet < -0.005) NeonRed else ElectricBlue
                                )
                            }
                        }

                        items(dayMatches, key = { it.id }) { item ->
                            TournamentHistoryCard(
                                item = item,
                                onEdit = { onEditClick(item.id) },
                                onDelete = { pendingDelete = item }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = NeonPurple,
            contentColor = TextPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 18.dp)
                .testTag("history_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addTournamentBtn)
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = CardDarkElevated,
            title = { Text(strings.deleteConfirmTitle, color = TextPrimary) },
            text = {
                Text(
                    "${item.name}\n\n${strings.deleteConfirmMessage}",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConfirm(item.id)
                        pendingDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text(strings.delete, color = NeonRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun TournamentHistoryCard(
    item: TournamentEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalStrings.current
    val statusColor = when {
        item.isProfit -> NeonGreen
        item.isLoss -> NeonRed
        else -> ElectricBlue
    }
    val statusLabel = when {
        item.isProfit -> strings.profitLabel
        item.isLoss -> strings.lossLabel
        else -> strings.breakEvenLabel
    }

    NeonGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${item.id}"),
        glowColor = statusColor.copy(alpha = 0.7f),
        cornerRadius = 18.dp,
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonPurple.copy(alpha = 0.22f))
                                .border(1.dp, NeonPurple.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.gameMode.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrightViolet
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor.copy(alpha = 0.16f))
                        .border(1.dp, statusColor.copy(alpha = 0.75f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$statusLabel ${formatTaka(item.netResult, showSign = true)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricMini(label = strings.entryLabel, value = formatTaka(item.entryFee))
                MetricMini(label = strings.expensesLabel, value = formatTaka(item.additionalExpenses))
                MetricMini(label = strings.totalInvestment, value = formatTaka(item.totalInvestment))
                MetricMini(label = strings.totalWinning, value = formatTaka(item.totalWinning), valueColor = NeonGreen)
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp).testTag("edit_tournament_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.edit,
                        tint = BrightViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp).testTag("delete_tournament_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = strings.delete,
                        tint = NeonRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricMini(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = valueColor
        )
    }
}
