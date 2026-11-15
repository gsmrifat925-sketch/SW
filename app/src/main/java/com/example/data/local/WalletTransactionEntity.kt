package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.WalletTxType
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "wallet_transactions",
    indices = [Index(value = ["tournamentId"])]
)
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: WalletTxType,
    val amount: Double,
    val description: String,
    val dateMillis: Long,
    val tournamentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
