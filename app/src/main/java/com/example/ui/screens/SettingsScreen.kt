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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.preferences.UserPreferences
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.ui.components.NeonFilterPill
import com.example.ui.components.NeonGlassCard
import com.example.ui.components.PinLockScreen
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onSaveTeamIdentity: (String, String) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onEnablePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onBackupJson: () -> Unit,
    onRestoreJson: () -> Unit,
    onLoadSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var teamNameInput by remember(preferences.teamName) { mutableStateOf(preferences.teamName) }
    var teamTaglineInput by remember(preferences.teamTagline) { mutableStateOf(preferences.teamTagline) }

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var firstPin by remember { mutableStateOf<String?>(null) }
    var pinSetupError by remember { mutableStateOf<String?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = OrbitronFontFamily,
                    color = TextPrimary
                )
            }

            // Team Identity Section
            item {
                NeonGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonPurple,
                    cornerRadius = 18.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.teamIdentitySection,
                            style = MaterialTheme.typography.titleMedium,
                            color = BrightViolet
                        )
                        OutlinedTextField(
                            value = teamNameInput,
                            onValueChange = { teamNameInput = it },
                            label = { Text(strings.teamNameLabel) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightViolet,
                                unfocusedBorderColor = NeonPurple.copy(alpha = 0.35f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("settings_team_name")
                        )
                        OutlinedTextField(
                            value = teamTaglineInput,
                            onValueChange = { teamTaglineInput = it },
                            label = { Text(strings.teamTaglineLabel) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrightViolet,
                                unfocusedBorderColor = NeonPurple.copy(alpha = 0.35f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("settings_team_tagline")
                        )
                        Button(
                            onClick = { onSaveTeamIdentity(teamNameInput, teamTaglineInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.align(Alignment.End).testTag("settings_save_team_btn")
                        ) {
                            Text(strings.confirm, color = TextPrimary)
                        }
                    }
                }
            }

            // Language Section (English / Bangla)
            item {
                NeonGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = ElectricBlue,
                    cornerRadius = 18.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = ElectricBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.languageSection,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                NeonFilterPill(
                                    label = lang.displayName,
                                    selected = preferences.language == lang,
                                    testTag = "lang_pill_${lang.code}",
                                    onClick = { onSelectLanguage(lang) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // PIN Lock Security Section
            item {
                NeonGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonPurple,
                    cornerRadius = 18.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BrightViolet)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.pinLockTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = strings.pinLockSubtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = preferences.pinEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        firstPin = null
                                        pinSetupError = null
                                        showPinSetupDialog = true
                                    } else {
                                        onDisablePin()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextPrimary,
                                    checkedTrackColor = NeonPurple
                                ),
                                modifier = Modifier.testTag("settings_pin_switch")
                            )
                        }
                        if (preferences.pinEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    firstPin = null
                                    pinSetupError = null
                                    showPinSetupDialog = true
                                },
                                modifier = Modifier.testTag("settings_change_pin_btn")
                            ) {
                                Text(strings.changePinBtn, color = ElectricBlue)
                            }
                        }
                    }
                }
            }

            // Exports, Backup & Restore Section
            item {
                NeonGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonGreen.copy(alpha = 0.7f),
                    cornerRadius = 18.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.dataBackupSection,
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonGreen
                        )
                        SettingsActionRow(
                            icon = Icons.Default.PictureAsPdf,
                            label = strings.exportPdf,
                            accent = NeonPurple,
                            testTag = "settings_export_pdf",
                            onClick = onExportPdf
                        )
                        SettingsActionRow(
                            icon = Icons.Default.TableChart,
                            label = strings.exportCsv,
                            accent = ElectricBlue,
                            testTag = "settings_export_csv",
                            onClick = onExportCsv
                        )
                        SettingsActionRow(
                            icon = Icons.Default.Backup,
                            label = strings.backupJsonBtn,
                            accent = NeonGreen,
                            testTag = "settings_backup_json",
                            onClick = onBackupJson
                        )
                        SettingsActionRow(
                            icon = Icons.Default.Restore,
                            label = strings.restoreJsonBtn,
                            accent = BrightViolet,
                            testTag = "settings_restore_json",
                            onClick = onRestoreJson
                        )
                        SettingsActionRow(
                            icon = Icons.Default.Science,
                            label = strings.loadSampleDataBtn,
                            accent = ElectricBlue,
                            testTag = "settings_load_sample",
                            onClick = onLoadSampleData
                        )
                        SettingsActionRow(
                            icon = Icons.Default.DeleteForever,
                            label = strings.clearAllDataBtn,
                            accent = NeonRed,
                            testTag = "settings_clear_all",
                            onClick = { showClearDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showPinSetupDialog) {
        Dialog(
            onDismissRequest = { showPinSetupDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PinLockScreen(
                title = if (firstPin == null) strings.createPinTitle else strings.confirmPinTitle,
                subtitle = strings.pinLockSubtitle,
                errorMessage = pinSetupError,
                showCancel = true,
                onCancel = { showPinSetupDialog = false },
                onPinComplete = { entered ->
                    if (firstPin == null) {
                        firstPin = entered
                        pinSetupError = null
                    } else if (firstPin == entered) {
                        onEnablePin(entered)
                        showPinSetupDialog = false
                    } else {
                        firstPin = null
                        pinSetupError = strings.pinMismatchError
                    }
                }
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CardDarkElevated,
            title = { Text(strings.clearAllDataBtn, color = NeonRed) },
            text = { Text(strings.clearAllConfirmMsg, color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData()
                        showClearDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_all_btn")
                ) {
                    Text(strings.delete, color = NeonRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(strings.cancel, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    label: String,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDarkElevated.copy(alpha = 0.75f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
    }
}
