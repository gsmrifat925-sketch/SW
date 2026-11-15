package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shockwave_prefs")

data class UserPreferences(
    val startingBalance: Double = 0.0,
    val language: AppLanguage = AppLanguage.EN,
    val pinEnabled: Boolean = false,
    val pinHash: String = "",
    val teamName: String = "SHOCKWAVE",
    val teamTagline: String = "Play • Win • Grow"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val STARTING_BALANCE = doublePreferencesKey("starting_balance")
        val LANGUAGE = stringPreferencesKey("app_language")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val TEAM_NAME = stringPreferencesKey("team_name")
        val TEAM_TAGLINE = stringPreferencesKey("team_tagline")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val langCode = prefs[Keys.LANGUAGE] ?: AppLanguage.EN.code
        UserPreferences(
            startingBalance = prefs[Keys.STARTING_BALANCE] ?: 0.0,
            language = if (langCode == AppLanguage.BN.code) AppLanguage.BN else AppLanguage.EN,
            pinEnabled = prefs[Keys.PIN_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            teamName = prefs[Keys.TEAM_NAME]?.ifBlank { "SHOCKWAVE" } ?: "SHOCKWAVE",
            teamTagline = prefs[Keys.TEAM_TAGLINE]?.ifBlank { "Play • Win • Grow" } ?: "Play • Win • Grow"
        )
    }

    suspend fun setStartingBalance(amount: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STARTING_BALANCE] = amount.coerceAtLeast(0.0)
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setTeamIdentity(name: String, tagline: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TEAM_NAME] = name.trim().ifBlank { "SHOCKWAVE" }
            prefs[Keys.TEAM_TAGLINE] = tagline.trim().ifBlank { "Play • Win • Grow" }
        }
    }

    suspend fun setPin(rawPin: String) {
        val hashed = hashPin(rawPin)
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_HASH] = hashed
            prefs[Keys.PIN_ENABLED] = true
        }
    }

    suspend fun disablePin() {
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_ENABLED] = false
            prefs[Keys.PIN_HASH] = ""
        }
    }

    fun verifyPin(rawPin: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        return hashPin(rawPin) == storedHash
    }

    companion object {
        fun hashPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256")
                .digest("shockwave_salt_$pin".toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
