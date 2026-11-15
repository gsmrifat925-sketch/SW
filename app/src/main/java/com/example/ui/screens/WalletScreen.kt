package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WalletTransactionEntity
import com.example.data.model.WalletTxType
import com.example.data.repository.WalletSummary
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.i18n.formatDateShort
import com.example.i18n.formatTaka
import com.example.ui.components.NeonGlassCard
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
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

private enum class WalletDialogMode {
    NONE,
    DEPOSIT,
    WITHDRAW,
    STARTING_BALANCE
}

@Composable
fun WalletScreen(
    walletSummary: WalletSummary,
    transactions: List<WalletTransactionEntity>,
    language: AppLanguage,
    onAddManualTx: (WalletTxType, Double, String) -> Unit,
    onUpdateStartingBalance: (Double) -> Unit,
    onDeleteManualTx: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var dialogMode by remember { mutableStateOf(WalletDialogMode.NONE) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = strings.walletTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = OrbitronFontFamily,
                    color = TextPrimary
                )
            }

            // Available Balance Hero Card
            item {
                val balColor = if (walletSummary.availableBalance >= 0) NeonGreen else NeonRed
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_balance_card"),
                    glowColor = NeonPurple,
                    containerBrush = Brush.linearGradient(
                        listOf(DeepPurpleCard, Color(0xFF100724), Color(0xFF0A1626))
                    ),
                    cornerRadius = 22.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.availableBalance,
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = BrightViolet
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatTaka(walletSummary.availableBalance),
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = balColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.walletSeparateNote,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )
                    }
                }
            }

            // Action Buttons: + Deposit / - Withdraw / Starting Fund
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WalletActionButton(
                        label = strings.depositBtn,
                        accent = NeonGreen,
                        testTag = "wallet_deposit_btn",
                        onClick = { dialogMode = WalletDialogMode.DEPOSIT },
                        modifier = Modifier.weight(1f)
                    )
                    WalletActionButton(
                        label = strings.withdrawBtn,
                        accent = NeonRed,
                        testTag = "wallet_withdraw_btn",
                        onClick = { dialogMode = WalletDialogMode.WITHDRAW },
                        modifier = Modifier.weight(1f)
                    )
                    WalletActionButton(
                        label = strings.setStartingBtn,
                        accent = BrightViolet,
                        icon = Icons.Default.Tune,
                        testTag = "wallet_starting_btn",
                        onClick = { dialogMode = WalletDialogMode.STARTING_BALANCE },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 6 Breakdown Metric Cards (2 columns x 3 rows)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WalletStatMini(
                            title = strings.startingBalance,
                            value = formatTaka(walletSummary.startingBalance),
                            accent = BrightViolet,
                            modifier = Modifier.weight(1f)
                        )
                        WalletStatMini(
                            title = strings.prizeMoneyReceived,
                            value = formatTaka(walletSummary.prizeMoneyReceived),
                            accent = NeonGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WalletStatMini(
                            title = strings.entryFeePayments,
                            value = formatTaka(walletSummary.entryFeePayments),
                            accent = NeonRed,
                            modifier = Modifier.weight(1f)
                        )
                        WalletStatMini(
                            title = strings.additionalExpenses,
                            value = formatTaka(walletSummary.additionalExpenses),
                            accent = NeonRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WalletStatMini(
                            title = strings.manualDeposits,
                            value = formatTaka(walletSummary.manualDeposits),
                            accent = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                        WalletStatMini(
                            title = strings.manualWithdrawals,
                            value = formatTaka(walletSummary.manualWithdrawals),
                            accent = NeonRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.walletLedgerTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            if (transactions.isEmpty()) {
                item {
                    NeonGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = NeonPurple.copy(alpha = 0.35f),
                        cornerRadius = 16.dp
                    ) {
                        Text(
                            text = strings.noWalletTx,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    WalletTxRow(
                        tx = tx,
                        language = language,
                        onDeleteManual = { onDeleteManualTx(tx.id) }
                    )
                }
            }
        }
    }

    if (dialogMode != WalletDialogMode.NONE) {
        WalletAmountDialog(
            mode = dialogMode,
            currentStarting = walletSummary.startingBalance,
            onDismiss = { dialogMode = WalletDialogMode.NONE },
            onConfirm = { amount, desc ->
                when (dialogMode) {
                    WalletDialogMode.DEPOSIT -> onAddManualTx(WalletTxType.DEPOSIT, amount, desc)
                    WalletDialogMode.WITHDRAW -> onAddManualTx(WalletTxType.WITHDRAWAL, amount, desc)
                    WalletDialogMode.STARTING_BALANCE -> onUpdateStartingBalance(amount)
                    WalletDialogMode.NONE -> Unit
                }
                dialogMode = WalletDialogMode.NONE
            }
        )
    }
}

@Composable
private fun WalletActionButton(
    label: String,
    accent: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WalletStatMini(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    NeonGlassCard(
        modifier = modifier,
        glowColor = accent.copy(alpha = 0.55f),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WalletTxRow(
    tx: WalletTransactionEntity,
    language: AppLanguage,
    onDeleteManual: () -> Unit
) {
    val strings = LocalStrings.current
    val isInflow = tx.type == WalletTxType.PRIZE_MONEY || tx.type == WalletTxType.DEPOSIT
    val color = if (isInflow) NeonGreen else NeonRed

    NeonGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_tx_${tx.id}"),
        glowColor = color.copy(alpha = 0.45f),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isInflow) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDateShort(tx.dateMillis, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    if (tx.tournamentId != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.linkedTournamentTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = BrightViolet
                        )
                    }
                }
            }
            Text(
                text = if (isInflow) "+${formatTaka(tx.amount)}" else "-${formatTaka(tx.amount)}",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
            if (tx.tournamentId == null) {
                IconButton(
                    onClick = onDeleteManual,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = strings.delete,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletAmountDialog(
    mode: WalletDialogMode,
    currentStarting: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    val strings = LocalStrings.current
    var amountText by remember {
        mutableStateOf(
            if (mode == WalletDialogMode.STARTING_BALANCE && currentStarting > 0.0) {
                currentStarting.toLong().toString()
            } else ""
        )
    }
    var descText by remember { mutableStateOf("") }

    val title = when (mode) {
        WalletDialogMode.DEPOSIT -> strings.depositBtn
        WalletDialogMode.WITHDRAW -> strings.withdrawBtn
        WalletDialogMode.STARTING_BALANCE -> strings.startingBalance
        WalletDialogMode.NONE -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkElevated,
        title = { Text(title, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw -> amountText = raw.filter { it.isDigit() || it == '.' } },
                    label = { Text(strings.amountLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrightViolet,
                        unfocusedBorderColor = NeonPurple.copy(alpha = 0.4f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("wallet_dialog_amount")
                )
                if (mode != WalletDialogMode.STARTING_BALANCE) {
                    OutlinedTextField(
                        value = descText,
                        onValueChange = { descText = it },
                        label = { Text(strings.descriptionLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrightViolet,
                            unfocusedBorderColor = NeonPurple.copy(alpha = 0.4f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("wallet_dialog_desc")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: return@TextButton
                    onConfirm(amt, descText)
                },
                modifier = Modifier.testTag("wallet_dialog_confirm")
            ) {
                Text(strings.confirm, color = NeonGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = TextSecondary)
            }
        }
    )
}
