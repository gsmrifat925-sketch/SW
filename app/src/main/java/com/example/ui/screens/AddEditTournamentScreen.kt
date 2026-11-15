package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TournamentEntity
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.i18n.formatDateShort
import com.example.i18n.formatTaka
import com.example.ui.components.NeonFilterPill
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.CardDarkSurface
import com.example.ui.theme.DeepCrimsonCard
import com.example.ui.theme.DeepEmeraldCard
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.DeepPurpleCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTournamentScreen(
    existingTournament: TournamentEntity?,
    language: AppLanguage,
    onBack: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        dateMillis: Long,
        gameMode: GameMode,
        entryFee: Double,
        totalWinning: Double,
        additionalExpenses: Double,
        matchResult: MatchResult,
        notes: String,
        syncWithWallet: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isEditing = existingTournament != null

    var name by remember { mutableStateOf(existingTournament?.name ?: "") }
    var dateMillis by remember { mutableLongStateOf(existingTournament?.dateMillis ?: System.currentTimeMillis()) }
    var gameMode by remember { mutableStateOf(existingTournament?.gameMode ?: GameMode.SQUAD) }
    var entryFeeText by remember { mutableStateOf(existingTournament?.entryFee?.cleanNumberString() ?: "") }
    var winningText by remember { mutableStateOf(existingTournament?.totalWinning?.cleanNumberString() ?: "") }
    var extraExpensesText by remember { mutableStateOf(existingTournament?.additionalExpenses?.cleanNumberString() ?: "") }
    var matchResult by remember { mutableStateOf(existingTournament?.matchResult ?: MatchResult.WIN) }
    var manuallyPickedResult by remember { mutableStateOf(existingTournament != null) }
    var notes by remember { mutableStateOf(existingTournament?.notes ?: "") }
    var syncWithWallet by remember { mutableStateOf(existingTournament?.syncWithWallet ?: true) }

    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(existingTournament?.id) {
        if (existingTournament != null) {
            name = existingTournament.name
            dateMillis = existingTournament.dateMillis
            gameMode = existingTournament.gameMode
            entryFeeText = existingTournament.entryFee.cleanNumberString()
            winningText = existingTournament.totalWinning.cleanNumberString()
            extraExpensesText = existingTournament.additionalExpenses.cleanNumberString()
            matchResult = existingTournament.matchResult
            manuallyPickedResult = true
            notes = existingTournament.notes
            syncWithWallet = existingTournament.syncWithWallet
        }
    }

    val entryFee = entryFeeText.toDoubleOrNull() ?: 0.0
    val winning = winningText.toDoubleOrNull() ?: 0.0
    val extraExpenses = extraExpensesText.toDoubleOrNull() ?: 0.0
    val totalInvestment = entryFee + extraExpenses
    val netResult = winning - totalInvestment

    // Auto-suggest Match Result when amounts change if user hasn't manually overridden it
    LaunchedEffect(netResult, manuallyPickedResult) {
        if (!manuallyPickedResult) {
            matchResult = when {
                netResult > 0.005 -> MatchResult.WIN
                netResult < -0.005 -> MatchResult.LOSS
                else -> MatchResult.BREAK_EVEN
            }
        }
    }

    val previewGlow = when {
        netResult > 0.005 -> NeonGreen
        netResult < -0.005 -> NeonRed
        else -> ElectricBlue
    }
    val previewBg = when {
        netResult > 0.005 -> listOf(DeepEmeraldCard, Color(0xFF041914))
        netResult < -0.005 -> listOf(DeepCrimsonCard, Color(0xFF1A050C))
        else -> listOf(DeepPurpleCard, Color(0xFF0D0821))
    }
    val previewStatusText = when {
        netResult > 0.005 -> strings.profitLabel.uppercase()
        netResult < -0.005 -> strings.lossLabel.uppercase()
        else -> strings.breakEvenLabel.uppercase()
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrightViolet,
        unfocusedBorderColor = NeonPurple.copy(alpha = 0.35f),
        focusedContainerColor = CardDarkElevated.copy(alpha = 0.72f),
        unfocusedContainerColor = CardDarkSurface.copy(alpha = 0.8f),
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = BrightViolet,
        unfocusedLabelColor = TextSecondary,
        cursorColor = ElectricBlue
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF120624), DeepObsidian, PureBlack))
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("add_tournament_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isEditing) strings.editTournamentTitle else strings.addTournamentTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = OrbitronFontFamily,
                    color = TextPrimary
                )
            }

            // Scrollable One-Handed Form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Profit/Loss Preview Card
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_profit_preview_card"),
                    glowColor = previewGlow,
                    containerBrush = Brush.linearGradient(previewBg),
                    cornerRadius = 18.dp
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
                            Text(
                                text = strings.livePreviewTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(previewGlow.copy(alpha = 0.2f))
                                    .border(1.dp, previewGlow.copy(alpha = 0.7f), RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = previewStatusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = previewGlow
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = strings.totalInvestment,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = formatTaka(totalInvestment),
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = strings.netResultLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = formatTaka(netResult, showSign = true),
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = previewGlow
                                )
                            }
                        }
                    }
                }

                // Tournament Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text(strings.tournamentName) },
                    placeholder = { Text(strings.tournamentNameHint, color = TextMuted) },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_tournament_name")
                )

                // Date Picker Row
                Column {
                    Text(
                        text = strings.dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeonFilterPill(
                            label = strings.filterToday,
                            selected = isSameDay(dateMillis, System.currentTimeMillis()),
                            testTag = "date_chip_today",
                            onClick = { dateMillis = System.currentTimeMillis() }
                        )
                        NeonFilterPill(
                            label = strings.yesterdayLabel,
                            selected = isSameDay(dateMillis, System.currentTimeMillis() - 86_400_000L),
                            testTag = "date_chip_yesterday",
                            onClick = { dateMillis = System.currentTimeMillis() - 86_400_000L }
                        )
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardDarkElevated)
                                .border(1.dp, NeonPurple.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("date_picker_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = strings.dateLabel,
                                tint = BrightViolet,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formatDateShort(dateMillis, language),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Game Mode Selector
                Column {
                    Text(
                        text = strings.gameModeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameMode.entries.forEach { mode ->
                            val label = when (mode) {
                                GameMode.SOLO -> strings.modeSolo
                                GameMode.DUO -> strings.modeDuo
                                GameMode.SQUAD -> strings.modeSquad
                                GameMode.CUSTOM -> strings.modeCustom
                            }
                            NeonFilterPill(
                                label = label,
                                selected = gameMode == mode,
                                testTag = "mode_chip_${mode.name.lowercase()}",
                                onClick = { gameMode = mode },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Financial Inputs (Combined Team Amounts)
                Text(
                    text = strings.teamCombinedHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBlue
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = entryFeeText,
                        onValueChange = {
                            entryFeeText = sanitizeAmountInput(it)
                            validationError = null
                        },
                        label = { Text(strings.teamEntryFee) },
                        singleLine = true,
                        colors = fieldColors,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_entry_fee")
                    )

                    OutlinedTextField(
                        value = winningText,
                        onValueChange = {
                            winningText = sanitizeAmountInput(it)
                            validationError = null
                        },
                        label = { Text(strings.totalTeamWinning) },
                        singleLine = true,
                        colors = fieldColors,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_total_winning")
                    )
                }

                OutlinedTextField(
                    value = extraExpensesText,
                    onValueChange = {
                        extraExpensesText = sanitizeAmountInput(it)
                        validationError = null
                    },
                    label = { Text(strings.additionalExpenses) },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_additional_expenses")
                )

                // Match Result Selector
                Column {
                    Text(
                        text = strings.matchResultLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MatchResult.entries.forEach { res ->
                            val lbl = when (res) {
                                MatchResult.WIN -> strings.resultWin
                                MatchResult.LOSS -> strings.resultLoss
                                MatchResult.BREAK_EVEN -> strings.resultBreakEven
                            }
                            NeonFilterPill(
                                label = lbl,
                                selected = matchResult == res,
                                testTag = "result_chip_${res.name.lowercase()}",
                                onClick = {
                                    manuallyPickedResult = true
                                    matchResult = res
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.notesLabel) },
                    placeholder = { Text(strings.notesHint, color = TextMuted) },
                    minLines = 2,
                    maxLines = 4,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes")
                )

                // Wallet Auto-Sync Toggle
                NeonGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonPurple.copy(alpha = 0.5f),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.syncWalletToggle,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = strings.syncWalletDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Switch(
                            checked = syncWithWallet,
                            onCheckedChange = { syncWithWallet = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = NeonPurple
                            ),
                            modifier = Modifier.testTag("switch_sync_wallet")
                        )
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // Sticky Bottom One-Handed Save Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureBlack.copy(alpha = 0.92f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                NeonPrimaryButton(
                    text = if (isEditing) strings.updateTournament else strings.saveTournament,
                    testTag = "save_tournament_btn",
                    onClick = {
                        if (isSaving) return@NeonPrimaryButton
                        if (name.isBlank()) {
                            validationError = strings.nameRequiredError
                            return@NeonPrimaryButton
                        }
                        if (entryFee < 0 || winning < 0 || extraExpenses < 0) {
                            validationError = strings.invalidAmountError
                            return@NeonPrimaryButton
                        }
                        isSaving = true
                        onSave(
                            existingTournament?.id ?: 0L,
                            name,
                            dateMillis,
                            gameMode,
                            entryFee,
                            winning,
                            extraExpenses,
                            matchResult,
                            notes,
                            syncWithWallet
                        )
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { dateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text(strings.confirm, color = BrightViolet)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun sanitizeAmountInput(input: String): String {
    val filtered = input.filter { it.isDigit() || it == '.' }
    val dotIndex = filtered.indexOf('.')
    return if (dotIndex >= 0) {
        val before = filtered.substring(0, dotIndex + 1)
        val after = filtered.substring(dotIndex + 1).replace(".", "").take(2)
        before + after
    } else {
        filtered
    }
}

private fun Double.cleanNumberString(): String {
    return if (this == 0.0) "" else if (this % 1.0 == 0.0) toLong().toString() else toString()
}

private fun isSameDay(a: Long, b: Long): Boolean {
    val calA = java.util.Calendar.getInstance().apply { timeInMillis = a }
    val calB = java.util.Calendar.getInstance().apply { timeInMillis = b }
    return calA.get(java.util.Calendar.YEAR) == calB.get(java.util.Calendar.YEAR) &&
        calA.get(java.util.Calendar.DAY_OF_YEAR) == calB.get(java.util.Calendar.DAY_OF_YEAR)
}
