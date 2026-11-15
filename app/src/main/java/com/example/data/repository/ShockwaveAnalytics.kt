package com.example.data.repository

import com.example.data.local.TournamentEntity
import com.example.data.local.WalletTransactionEntity
import com.example.data.model.DateFilter
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import com.example.data.model.ReportPeriod
import com.example.data.model.WalletTxType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class DashboardSummary(
    val totalInvestment: Double = 0.0,
    val totalWinning: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val netPnL: Double = 0.0,
    val totalMatches: Int = 0,
    val winningMatches: Int = 0,
    val losingMatches: Int = 0,
    val breakEvenMatches: Int = 0
)

data class ChartPoint(
    val label: String,
    val fullDateLabel: String,
    val profit: Double,
    val loss: Double,
    val matchCount: Int
)

data class WalletSummary(
    val startingBalance: Double = 0.0,
    val prizeMoneyReceived: Double = 0.0,
    val entryFeePayments: Double = 0.0,
    val additionalExpenses: Double = 0.0,
    val manualDeposits: Double = 0.0,
    val manualWithdrawals: Double = 0.0
) {
    val availableBalance: Double
        get() = startingBalance + prizeMoneyReceived + manualDeposits -
            entryFeePayments - additionalExpenses - manualWithdrawals
}

data class PeriodReportRow(
    val periodKey: String,
    val periodTitle: String,
    val sortKey: Long,
    val matchCount: Int,
    val winCount: Int,
    val lossCount: Int,
    val totalInvestment: Double,
    val totalWinning: Double,
    val totalProfit: Double,
    val totalLoss: Double,
    val netPnL: Double
)

data class ModeBreakdown(
    val mode: GameMode,
    val matchCount: Int,
    val wins: Int,
    val investment: Double,
    val winning: Double,
    val netPnL: Double
)

data class AllTimeStats(
    val summary: DashboardSummary = DashboardSummary(),
    val winRatePercent: Double = 0.0,
    val roiPercent: Double = 0.0,
    val avgInvestment: Double = 0.0,
    val avgWinning: Double = 0.0,
    val bestTournament: TournamentEntity? = null,
    val worstTournament: TournamentEntity? = null,
    val modeBreakdowns: List<ModeBreakdown> = emptyList()
)

data class CommandAlert(
    val id: String,
    val title: String,
    val message: String,
    val isWarning: Boolean = false
)

object ShockwaveAnalytics {

    fun filterByDate(
        tournaments: List<TournamentEntity>,
        filter: DateFilter,
        nowMillis: Long = System.currentTimeMillis()
    ): List<TournamentEntity> {
        if (filter == DateFilter.ALL_TIME) return tournaments
        val startMillis = startOfRangeMillis(filter, nowMillis)
        return tournaments.filter { it.dateMillis >= startMillis }
    }

    fun startOfRangeMillis(filter: DateFilter, nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return when (filter) {
            DateFilter.TODAY -> cal.timeInMillis
            DateFilter.SEVEN_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.timeInMillis
            }
            DateFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.timeInMillis
            }
            DateFilter.ALL_TIME -> 0L
        }
    }

    fun computeDashboardSummary(tournaments: List<TournamentEntity>): DashboardSummary {
        var totalInvestment = 0.0
        var totalWinning = 0.0
        var totalProfit = 0.0
        var totalLoss = 0.0
        var wins = 0
        var losses = 0
        var breakEvens = 0

        for (t in tournaments) {
            totalInvestment += t.totalInvestment
            totalWinning += t.totalWinning
            val net = t.netResult
            if (net > 0.005) {
                totalProfit += net
            } else if (net < -0.005) {
                totalLoss += abs(net)
            }
            when (t.matchResult) {
                MatchResult.WIN -> wins++
                MatchResult.LOSS -> losses++
                MatchResult.BREAK_EVEN -> breakEvens++
            }
        }

        return DashboardSummary(
            totalInvestment = totalInvestment,
            totalWinning = totalWinning,
            totalProfit = totalProfit,
            totalLoss = totalLoss,
            netPnL = totalWinning - totalInvestment,
            totalMatches = tournaments.size,
            winningMatches = wins,
            losingMatches = losses,
            breakEvenMatches = breakEvens
        )
    }

    fun buildChartPoints(
        tournaments: List<TournamentEntity>,
        filter: DateFilter,
        nowMillis: Long = System.currentTimeMillis()
    ): List<ChartPoint> {
        val filtered = filterByDate(tournaments, filter, nowMillis)
            .sortedBy { it.dateMillis }
        if (filtered.isEmpty()) return emptyList()

        return when (filter) {
            DateFilter.TODAY -> {
                // Show each match chronologically on Today view so single-day trends look crisp
                val timeFmt = SimpleDateFormat("HH:mm", Locale.ENGLISH)
                filtered.mapIndexed { idx, t ->
                    val p = if (t.netResult > 0.005) t.netResult else 0.0
                    val l = if (t.netResult < -0.005) abs(t.netResult) else 0.0
                    ChartPoint(
                        label = "#${idx + 1}",
                        fullDateLabel = "${t.name} (${timeFmt.format(Date(t.dateMillis))})",
                        profit = p,
                        loss = l,
                        matchCount = 1
                    )
                }
            }
            DateFilter.SEVEN_DAYS, DateFilter.THIS_MONTH -> {
                val keyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val shortFmt = SimpleDateFormat("dd", Locale.ENGLISH)
                val fullFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                filtered.groupBy { keyFmt.format(Date(it.dateMillis)) }
                    .entries
                    .sortedBy { it.key }
                    .map { (_, items) ->
                        val date = Date(items.first().dateMillis)
                        val profitSum = items.sumOf { if (it.netResult > 0.005) it.netResult else 0.0 }
                        val lossSum = items.sumOf { if (it.netResult < -0.005) abs(it.netResult) else 0.0 }
                        ChartPoint(
                            label = shortFmt.format(date),
                            fullDateLabel = fullFmt.format(date),
                            profit = profitSum,
                            loss = lossSum,
                            matchCount = items.size
                        )
                    }
            }
            DateFilter.ALL_TIME -> {
                val keyFmt = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
                val shortFmt = SimpleDateFormat("MMM", Locale.ENGLISH)
                val fullFmt = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
                filtered.groupBy { keyFmt.format(Date(it.dateMillis)) }
                    .entries
                    .sortedBy { it.key }
                    .map { (_, items) ->
                        val date = Date(items.first().dateMillis)
                        val profitSum = items.sumOf { if (it.netResult > 0.005) it.netResult else 0.0 }
                        val lossSum = items.sumOf { if (it.netResult < -0.005) abs(it.netResult) else 0.0 }
                        ChartPoint(
                            label = shortFmt.format(date),
                            fullDateLabel = fullFmt.format(date),
                            profit = profitSum,
                            loss = lossSum,
                            matchCount = items.size
                        )
                    }
            }
        }
    }

    fun computeWalletSummary(
        startingBalance: Double,
        transactions: List<WalletTransactionEntity>
    ): WalletSummary {
        var prizes = 0.0
        var entries = 0.0
        var expenses = 0.0
        var deposits = 0.0
        var withdrawals = 0.0

        for (tx in transactions) {
            when (tx.type) {
                WalletTxType.PRIZE_MONEY -> prizes += tx.amount
                WalletTxType.ENTRY_FEE -> entries += tx.amount
                WalletTxType.ADDITIONAL_EXPENSE -> expenses += tx.amount
                WalletTxType.DEPOSIT -> deposits += tx.amount
                WalletTxType.WITHDRAWAL -> withdrawals += tx.amount
            }
        }

        return WalletSummary(
            startingBalance = startingBalance,
            prizeMoneyReceived = prizes,
            entryFeePayments = entries,
            additionalExpenses = expenses,
            manualDeposits = deposits,
            manualWithdrawals = withdrawals
        )
    }

    fun computePeriodReports(
        tournaments: List<TournamentEntity>,
        period: ReportPeriod
    ): List<PeriodReportRow> {
        if (tournaments.isEmpty() || period == ReportPeriod.ALL_TIME) return emptyList()

        val grouped = tournaments.groupBy { t ->
            val cal = Calendar.getInstance().apply { timeInMillis = t.dateMillis }
            when (period) {
                ReportPeriod.DAILY -> {
                    val key = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(t.dateMillis))
                    val title = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(t.dateMillis))
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    Triple(key, title, cal.timeInMillis)
                }
                ReportPeriod.WEEKLY -> {
                    val weekYear = cal.get(Calendar.YEAR)
                    val weekNum = cal.get(Calendar.WEEK_OF_YEAR)
                    val key = "$weekYear-W${weekNum.toString().padStart(2, '0')}"
                    val weekStart = Calendar.getInstance().apply {
                        timeInMillis = t.dateMillis
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val fmt = SimpleDateFormat("dd MMM", Locale.ENGLISH)
                    val weekEndMillis = weekStart.timeInMillis + 6L * 24L * 3600L * 1000L
                    val title = "Week $weekNum (${fmt.format(Date(weekStart.timeInMillis))} - ${fmt.format(Date(weekEndMillis))})"
                    Triple(key, title, weekStart.timeInMillis)
                }
                ReportPeriod.MONTHLY -> {
                    val key = SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date(t.dateMillis))
                    val title = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date(t.dateMillis))
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    Triple(key, title, cal.timeInMillis)
                }
                ReportPeriod.YEARLY -> {
                    val year = cal.get(Calendar.YEAR)
                    Triple(year.toString(), year.toString(), year.toLong())
                }
                ReportPeriod.ALL_TIME -> Triple("ALL", "All Time", 0L)
            }
        }

        return grouped.entries.map { (meta, list) ->
            val summary = computeDashboardSummary(list)
            PeriodReportRow(
                periodKey = meta.first,
                periodTitle = meta.second,
                sortKey = meta.third,
                matchCount = summary.totalMatches,
                winCount = summary.winningMatches,
                lossCount = summary.losingMatches,
                totalInvestment = summary.totalInvestment,
                totalWinning = summary.totalWinning,
                totalProfit = summary.totalProfit,
                totalLoss = summary.totalLoss,
                netPnL = summary.netPnL
            )
        }.sortedByDescending { it.sortKey }
    }

    fun computeAllTimeStats(tournaments: List<TournamentEntity>): AllTimeStats {
        if (tournaments.isEmpty()) return AllTimeStats()
        val summary = computeDashboardSummary(tournaments)
        val winRate = if (summary.totalMatches > 0) {
            (summary.winningMatches.toDouble() / summary.totalMatches.toDouble()) * 100.0
        } else 0.0
        val roi = if (summary.totalInvestment > 0.0) {
            (summary.netPnL / summary.totalInvestment) * 100.0
        } else 0.0

        val modeBreakdowns = GameMode.entries.mapNotNull { mode ->
            val list = tournaments.filter { it.gameMode == mode }
            if (list.isEmpty()) null
            else {
                val mSum = computeDashboardSummary(list)
                ModeBreakdown(
                    mode = mode,
                    matchCount = mSum.totalMatches,
                    wins = mSum.winningMatches,
                    investment = mSum.totalInvestment,
                    winning = mSum.totalWinning,
                    netPnL = mSum.netPnL
                )
            }
        }

        return AllTimeStats(
            summary = summary,
            winRatePercent = winRate,
            roiPercent = roi,
            avgInvestment = summary.totalInvestment / tournaments.size,
            avgWinning = summary.totalWinning / tournaments.size,
            bestTournament = tournaments.maxByOrNull { it.netResult },
            worstTournament = tournaments.minByOrNull { it.netResult },
            modeBreakdowns = modeBreakdowns
        )
    }

    fun deriveCommandAlerts(
        tournaments: List<TournamentEntity>,
        walletSummary: WalletSummary
    ): List<CommandAlert> {
        val alerts = mutableListOf<CommandAlert>()
        if (walletSummary.availableBalance < 0.0) {
            alerts.add(
                CommandAlert(
                    id = "wallet_negative",
                    title = "Negative Wallet Balance",
                    message = "Team wallet balance is below ৳0. Consider recording a deposit or adjusting starting balance.",
                    isWarning = true
                )
            )
        }
        val recent = tournaments.take(3)
        if (recent.size == 3 && recent.all { it.isLoss }) {
            alerts.add(
                CommandAlert(
                    id = "loss_streak",
                    title = "3-Match Loss Streak",
                    message = "Last 3 tournaments ended in net loss. Review entry fees vs placements in Reports.",
                    isWarning = true
                )
            )
        } else if (recent.size == 3 && recent.all { it.isProfit }) {
            alerts.add(
                CommandAlert(
                    id = "win_streak",
                    title = "Hot Streak Active!",
                    message = "SHOCKWAVE closed the last 3 consecutive tournaments in profit.",
                    isWarning = false
                )
            )
        }
        tournaments.firstOrNull()?.let { latest ->
            val tag = if (latest.isProfit) "Profit" else if (latest.isLoss) "Loss" else "Break Even"
            alerts.add(
                CommandAlert(
                    id = "latest_${latest.id}",
                    title = "Latest: ${latest.name}",
                    message = "Recorded $tag (${latest.gameMode.name}) • Winning: ৳${latest.totalWinning.toInt()}",
                    isWarning = latest.isLoss
                )
            )
        }
        return alerts
    }
}
