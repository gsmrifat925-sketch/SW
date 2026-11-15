package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.WalletTxType
import kotlinx.coroutines.flow.Flow

@Dao
interface ShockwaveDao {
    @Query("SELECT * FROM tournaments ORDER BY dateMillis DESC, id DESC")
    fun observeAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments ORDER BY dateMillis DESC, id DESC")
    suspend fun getAllTournamentsOnce(): List<TournamentEntity>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getTournamentById(id: Long): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournamentById(id: Long)

    @Query("DELETE FROM tournaments")
    suspend fun deleteAllTournaments()

    @Query("SELECT * FROM wallet_transactions ORDER BY dateMillis DESC, id DESC")
    fun observeAllWalletTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY dateMillis DESC, id DESC")
    suspend fun getAllWalletTransactionsOnce(): List<WalletTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletTransaction(tx: WalletTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletTransactions(txs: List<WalletTransactionEntity>)

    @Query("DELETE FROM wallet_transactions WHERE id = :id")
    suspend fun deleteWalletTransactionById(id: Long)

    @Query("DELETE FROM wallet_transactions WHERE tournamentId = :tournamentId")
    suspend fun deleteWalletTransactionsByTournamentId(tournamentId: Long)

    @Query("DELETE FROM wallet_transactions")
    suspend fun deleteAllWalletTransactions()

    @Transaction
    suspend fun saveTournamentWithWalletSync(tournament: TournamentEntity): Long {
        val now = System.currentTimeMillis()
        val savedId = if (tournament.id == 0L) {
            insertTournament(tournament.copy(createdAt = now, updatedAt = now))
        } else {
            updateTournament(tournament.copy(updatedAt = now))
            tournament.id
        }

        // Always remove previously linked wallet rows first to prevent duplicate expenses or prizes
        deleteWalletTransactionsByTournamentId(savedId)

        if (tournament.syncWithWallet) {
            val linked = buildList {
                if (tournament.entryFee > 0.0) {
                    add(
                        WalletTransactionEntity(
                            type = WalletTxType.ENTRY_FEE,
                            amount = tournament.entryFee,
                            description = "${tournament.name} • Entry Fee",
                            dateMillis = tournament.dateMillis,
                            tournamentId = savedId,
                            createdAt = now
                        )
                    )
                }
                if (tournament.additionalExpenses > 0.0) {
                    add(
                        WalletTransactionEntity(
                            type = WalletTxType.ADDITIONAL_EXPENSE,
                            amount = tournament.additionalExpenses,
                            description = "${tournament.name} • Extra Expenses",
                            dateMillis = tournament.dateMillis,
                            tournamentId = savedId,
                            createdAt = now
                        )
                    )
                }
                if (tournament.totalWinning > 0.0) {
                    add(
                        WalletTransactionEntity(
                            type = WalletTxType.PRIZE_MONEY,
                            amount = tournament.totalWinning,
                            description = "${tournament.name} • Prize Money",
                            dateMillis = tournament.dateMillis,
                            tournamentId = savedId,
                            createdAt = now
                        )
                    )
                }
            }
            if (linked.isNotEmpty()) {
                insertWalletTransactions(linked)
            }
        }
        return savedId
    }

    @Transaction
    suspend fun deleteTournamentWithWalletSync(tournamentId: Long) {
        deleteWalletTransactionsByTournamentId(tournamentId)
        deleteTournamentById(tournamentId)
    }

    @Transaction
    suspend fun clearAll() {
        deleteAllWalletTransactions()
        deleteAllTournaments()
    }

    @Transaction
    suspend fun replaceBackup(
        tournaments: List<TournamentEntity>,
        transactions: List<WalletTransactionEntity>
    ) {
        clearAll()
        mergeBackup(tournaments, transactions)
    }

    @Transaction
    suspend fun mergeBackup(
        tournaments: List<TournamentEntity>,
        transactions: List<WalletTransactionEntity>
    ) {
        val idMapping = mutableMapOf<Long, Long>()
        tournaments.forEach { old ->
            val newId = insertTournament(old.copy(id = 0L))
            idMapping[old.id] = newId
        }
        val mappedTxs = transactions.map { tx ->
            val mappedTournamentId = tx.tournamentId?.let { oldTid -> idMapping[oldTid] }
            tx.copy(id = 0L, tournamentId = mappedTournamentId)
        }
        if (mappedTxs.isNotEmpty()) {
            insertWalletTransactions(mappedTxs)
        }
    }
}
