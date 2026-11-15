package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import com.example.data.model.WalletTxType

class Converters {
    @TypeConverter
    fun fromGameMode(mode: GameMode): String = mode.name

    @TypeConverter
    fun toGameMode(value: String): GameMode =
        runCatching { GameMode.valueOf(value) }.getOrDefault(GameMode.SQUAD)

    @TypeConverter
    fun fromMatchResult(result: MatchResult): String = result.name

    @TypeConverter
    fun toMatchResult(value: String): MatchResult =
        runCatching { MatchResult.valueOf(value) }.getOrDefault(MatchResult.WIN)

    @TypeConverter
    fun fromWalletTxType(type: WalletTxType): String = type.name

    @TypeConverter
    fun toWalletTxType(value: String): WalletTxType =
        runCatching { WalletTxType.valueOf(value) }.getOrDefault(WalletTxType.DEPOSIT)
}
