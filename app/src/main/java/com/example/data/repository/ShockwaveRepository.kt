package com.example.data.repository

import com.example.data.local.ShockwaveDao
import com.example.data.local.TournamentEntity
import com.example.data.local.WalletTransactionEntity
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import com.example.data.model.WalletTxType
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow

class ShockwaveRepository(
    private val dao: ShockwaveDao,
    private val prefsRepo: UserPreferencesRepository
) {
    val tournamentsFlow: Flow<List<TournamentEntity>> = dao.observeAllTournaments()
    val walletTransactionsFlow: Flow<List<WalletTransactionEntity>> = dao.observeAllWalletTransactions()
    val preferencesFlow: Flow<UserPreferences> = prefsRepo.preferencesFlow

    suspend fun saveTournament(tournament: TournamentEntity): Long {
        return dao.saveTournamentWithWalletSync(tournament)
    }

    suspend fun deleteTournament(id: Long) {
        dao.deleteTournamentWithWalletSync(id)
    }

    suspend fun getTournamentById(id: Long): TournamentEntity? = dao.getTournamentById(id)

    suspend fun addManualWalletTransaction(
        type: WalletTxType,
        amount: Double,
        description: String,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                type = type,
                amount = amount.coerceAtLeast(0.0),
                description = description.trim(),
                dateMillis = dateMillis,
                tournamentId = null
            )
        )
    }

    suspend fun deleteWalletTransaction(id: Long) {
        dao.deleteWalletTransactionById(id)
    }

    suspend fun setStartingBalance(amount: Double) = prefsRepo.setStartingBalance(amount)

    suspend fun setLanguage(language: AppLanguage) = prefsRepo.setLanguage(language)

    suspend fun setTeamIdentity(name: String, tagline: String) =
        prefsRepo.setTeamIdentity(name, tagline)

    suspend fun setPin(rawPin: String) = prefsRepo.setPin(rawPin)

    suspend fun disablePin() = prefsRepo.disablePin()

    fun verifyPin(rawPin: String, storedHash: String): Boolean =
        prefsRepo.verifyPin(rawPin, storedHash)

    suspend fun getAllTournamentsOnce(): List<TournamentEntity> = dao.getAllTournamentsOnce()

    suspend fun getAllWalletTransactionsOnce(): List<WalletTransactionEntity> =
        dao.getAllWalletTransactionsOnce()

    suspend fun restoreBackup(
        tournaments: List<TournamentEntity>,
        transactions: List<WalletTransactionEntity>,
        startingBalance: Double,
        teamName: String,
        teamTagline: String,
        replaceExisting: Boolean
    ) {
        if (replaceExisting) {
            dao.replaceBackup(tournaments, transactions)
            prefsRepo.setStartingBalance(startingBalance)
            prefsRepo.setTeamIdentity(teamName, teamTagline)
        } else {
            dao.mergeBackup(tournaments, transactions)
        }
    }

    suspend fun clearAllData() {
        dao.clearAll()
        prefsRepo.setStartingBalance(0.0)
    }

    /** Optional user-triggered sample Free Fire team matches for quick preview/evaluation. */
    suspend fun loadSampleTeamData() {
        val now = System.currentTimeMillis()
        val day = 24L * 3600L * 1000L
        prefsRepo.setStartingBalance(5000.0)

        val samples = listOf(
            TournamentEntity(
                name = "FF Dhaka Premier Scrim #12",
                dateMillis = now - 2 * 3600 * 1000L,
                gameMode = GameMode.SQUAD,
                entryFee = 800.0,
                totalWinning = 2500.0,
                additionalExpenses = 200.0,
                matchResult = MatchResult.WIN,
                notes = "Booyah on Bermuda + 14 team kills",
                syncWithWallet = true
            ),
            TournamentEntity(
                name = "Midnight Rush Duo Cup",
                dateMillis = now - 1 * day,
                gameMode = GameMode.DUO,
                entryFee = 400.0,
                totalWinning = 1200.0,
                additionalExpenses = 50.0,
                matchResult = MatchResult.WIN,
                notes = "1st place Purgatory",
                syncWithWallet = true
            ),
            TournamentEntity(
                name = "Chittagong Clash Custom",
                dateMillis = now - 3 * day,
                gameMode = GameMode.CUSTOM,
                entryFee = 600.0,
                totalWinning = 0.0,
                additionalExpenses = 100.0,
                matchResult = MatchResult.LOSS,
                notes = "4v4 Room vs AlphaX — lost 5-7",
                syncWithWallet = true
            ),
            TournamentEntity(
                name = "Bengal Elite Squad Series",
                dateMillis = now - 5 * day,
                gameMode = GameMode.SQUAD,
                entryFee = 1000.0,
                totalWinning = 3200.0,
                additionalExpenses = 250.0,
                matchResult = MatchResult.WIN,
                notes = "2nd overall + MVP bonus",
                syncWithWallet = true
            ),
            TournamentEntity(
                name = "Weekend Solo Showdown",
                dateMillis = now - 9 * day,
                gameMode = GameMode.SOLO,
                entryFee = 250.0,
                totalWinning = 600.0,
                additionalExpenses = 0.0,
                matchResult = MatchResult.WIN,
                notes = "Top 3 finish",
                syncWithWallet = true
            ),
            TournamentEntity(
                name = "Sylhet Pro Qualifier",
                dateMillis = now - 14 * day,
                gameMode = GameMode.SQUAD,
                entryFee = 1200.0,
                totalWinning = 500.0,
                additionalExpenses = 150.0,
                matchResult = MatchResult.LOSS,
                notes = "3rd party rotate in Zone 4",
                syncWithWallet = true
            )
        )
        samples.forEach { dao.saveTournamentWithWalletSync(it) }
    }
}
