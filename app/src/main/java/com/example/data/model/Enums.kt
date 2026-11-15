package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode {
    SOLO,
    DUO,
    SQUAD,
    CUSTOM
}

@Serializable
enum class MatchResult {
    WIN,
    LOSS,
    BREAK_EVEN
}

enum class DateFilter {
    TODAY,
    SEVEN_DAYS,
    THIS_MONTH,
    ALL_TIME
}

enum class ReportPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    ALL_TIME
}

enum class OutcomeFilter {
    ALL,
    PROFIT,
    LOSS,
    BREAK_EVEN
}

@Serializable
enum class WalletTxType {
    PRIZE_MONEY,
    ENTRY_FEE,
    ADDITIONAL_EXPENSE,
    DEPOSIT,
    WITHDRAWAL
}
