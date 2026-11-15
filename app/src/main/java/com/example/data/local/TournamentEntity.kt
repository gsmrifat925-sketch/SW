package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val dateMillis: Long,
    val gameMode: GameMode,
    val entryFee: Double,
    val totalWinning: Double,
    val additionalExpenses: Double,
    val matchResult: MatchResult,
    val notes: String = "",
    val syncWithWallet: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Total combined team investment — never multiplied by player count. */
    val totalInvestment: Double
        get() = entryFee + additionalExpenses

    /** Net accounting result = Total Team Winning - Total Investment. */
    val netResult: Double
        get() = totalWinning - totalInvestment

    val isProfit: Boolean
        get() = netResult > 0.005

    val isLoss: Boolean
        get() = netResult < -0.005

    val isBreakEven: Boolean
        get() = !isProfit && !isLoss
}
