package com.example.data.export

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.local.TournamentEntity
import com.example.data.local.WalletTransactionEntity
import com.example.data.repository.DashboardSummary
import com.example.data.repository.WalletSummary
import com.example.i18n.formatTaka
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class ShockwaveBackupPayload(
    val version: Int = 1,
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val teamName: String = "SHOCKWAVE",
    val teamTagline: String = "Play • Win • Grow",
    val startingBalance: Double = 0.0,
    val tournaments: List<TournamentEntity> = emptyList(),
    val walletTransactions: List<WalletTransactionEntity> = emptyList()
)

object ExportBackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun exportJsonBackup(
        tournaments: List<TournamentEntity>,
        transactions: List<WalletTransactionEntity>,
        startingBalance: Double,
        teamName: String,
        teamTagline: String
    ): String {
        val payload = ShockwaveBackupPayload(
            teamName = teamName,
            teamTagline = teamTagline,
            startingBalance = startingBalance,
            tournaments = tournaments,
            walletTransactions = transactions
        )
        return json.encodeToString(payload)
    }

    fun parseJsonBackup(rawJson: String): ShockwaveBackupPayload {
        return json.decodeFromString<ShockwaveBackupPayload>(rawJson)
    }

    fun generateCsv(
        tournaments: List<TournamentEntity>,
        walletSummary: WalletSummary
    ): String {
        val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        val sb = StringBuilder()
        sb.appendLine("Tournament ID,Name,Date,Game Mode,Team Entry Fee (BDT),Additional Expenses (BDT),Total Investment (BDT),Total Team Winning (BDT),Net Result (BDT),Status,Match Result,Wallet Synced,Notes")
        tournaments.forEach { t ->
            val status = when {
                t.isProfit -> "PROFIT"
                t.isLoss -> "LOSS"
                else -> "BREAK_EVEN"
            }
            sb.appendLine(
                listOf(
                    t.id.toString(),
                    csvEscape(t.name),
                    csvEscape(dateFmt.format(Date(t.dateMillis))),
                    t.gameMode.name,
                    t.entryFee.toString(),
                    t.additionalExpenses.toString(),
                    t.totalInvestment.toString(),
                    t.totalWinning.toString(),
                    t.netResult.toString(),
                    status,
                    t.matchResult.name,
                    t.syncWithWallet.toString(),
                    csvEscape(t.notes)
                ).joinToString(",")
            )
        }
        sb.appendLine()
        sb.appendLine("WALLET SUMMARY,Amount (BDT)")
        sb.appendLine("Starting Balance,${walletSummary.startingBalance}")
        sb.appendLine("Prize Money Received,${walletSummary.prizeMoneyReceived}")
        sb.appendLine("Manual Deposits,${walletSummary.manualDeposits}")
        sb.appendLine("Entry Fee Payments,${walletSummary.entryFeePayments}")
        sb.appendLine("Additional Expenses,${walletSummary.additionalExpenses}")
        sb.appendLine("Manual Withdrawals,${walletSummary.manualWithdrawals}")
        sb.appendLine("Available Balance,${walletSummary.availableBalance}")
        return sb.toString()
    }

    fun writePdfReport(
        outputStream: OutputStream,
        teamName: String,
        periodLabel: String,
        tournaments: List<TournamentEntity>,
        summary: DashboardSummary,
        walletSummary: WalletSummary
    ) {
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        var pageNum = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = pdf.startPage(pageInfo)
        var canvas = page.canvas

        val bgPaint = Paint().apply { color = Color.parseColor("#080612") }
        val cardPaint = Paint().apply { color = Color.parseColor("#140E26") }
        val accentPaint = Paint().apply {
            color = Color.parseColor("#9D4EDD")
            strokeWidth = 2f
        }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint().apply {
            color = Color.parseColor("#B8AEE0")
            textSize = 11f
        }
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#C77DFF")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10.5f
        }
        val greenPaint = Paint().apply {
            color = Color.parseColor("#00FFA3")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val redPaint = Paint().apply {
            color = Color.parseColor("#FF3B6B")
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        fun drawPageHeader() {
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
            canvas.drawText("$teamName — FINANCIAL REPORT", margin, 48f, titlePaint)
            val stamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(Date())
            canvas.drawText("FREE FIRE TEAM TRACKER  •  Period: $periodLabel  •  Generated: $stamp", margin, 68f, subPaint)
            canvas.drawLine(margin, 80f, pageWidth - margin, 80f, accentPaint)
        }

        drawPageHeader()

        // Summary Box
        canvas.drawRoundRect(margin, 94f, pageWidth - margin, 186f, 12f, 12f, cardPaint)
        canvas.drawText("TOURNAMENT ACCOUNTING SUMMARY", margin + 14f, 114f, sectionPaint)
        canvas.drawText("Matches: ${summary.totalMatches}  (Wins: ${summary.winningMatches} / Losses: ${summary.losingMatches})", margin + 14f, 134f, bodyPaint)
        canvas.drawText("Total Investment: ${formatTaka(summary.totalInvestment)}", margin + 14f, 152f, bodyPaint)
        canvas.drawText("Total Winning: ${formatTaka(summary.totalWinning)}", margin + 270f, 152f, bodyPaint)
        canvas.drawText("Total Profit: ${formatTaka(summary.totalProfit)}", margin + 14f, 170f, greenPaint)
        canvas.drawText("Total Loss: ${formatTaka(summary.totalLoss)}", margin + 270f, 170f, redPaint)

        // Wallet Box
        canvas.drawRoundRect(margin, 198f, pageWidth - margin, 260f, 12f, 12f, cardPaint)
        canvas.drawText("TEAM WALLET SNAPSHOT", margin + 14f, 218f, sectionPaint)
        canvas.drawText("Available Balance: ${formatTaka(walletSummary.availableBalance)}", margin + 14f, 238f, greenPaint)
        canvas.drawText("Starting: ${formatTaka(walletSummary.startingBalance)}  |  Deposits: ${formatTaka(walletSummary.manualDeposits)}  |  Withdrawals: ${formatTaka(walletSummary.manualWithdrawals)}", margin + 14f, 253f, subPaint)

        // Table Header
        var y = 290f
        canvas.drawText("TOURNAMENT LEDGER (${tournaments.size})", margin, y, sectionPaint)
        y += 18f
        canvas.drawText("Date", margin, y, subPaint)
        canvas.drawText("Tournament", margin + 78f, y, subPaint)
        canvas.drawText("Mode", margin + 245f, y, subPaint)
        canvas.drawText("Invest", margin + 305f, y, subPaint)
        canvas.drawText("Winning", margin + 375f, y, subPaint)
        canvas.drawText("Net P&L", margin + 450f, y, subPaint)
        y += 8f
        canvas.drawLine(margin, y, pageWidth - margin, y, accentPaint)
        y += 16f

        val dateFmt = SimpleDateFormat("dd MMM yy", Locale.ENGLISH)
        for (t in tournaments) {
            if (y > pageHeight - 48f) {
                pdf.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                page = pdf.startPage(pageInfo)
                canvas = page.canvas
                drawPageHeader()
                y = 104f
            }
            val nameTrimmed = if (t.name.length > 26) t.name.take(24) + ".." else t.name
            canvas.drawText(dateFmt.format(Date(t.dateMillis)), margin, y, bodyPaint)
            canvas.drawText(nameTrimmed, margin + 78f, y, bodyPaint)
            canvas.drawText(t.gameMode.name, margin + 245f, y, subPaint)
            canvas.drawText(formatTaka(t.totalInvestment), margin + 305f, y, bodyPaint)
            canvas.drawText(formatTaka(t.totalWinning), margin + 375f, y, bodyPaint)
            val netPaint = when {
                t.isProfit -> greenPaint
                t.isLoss -> redPaint
                else -> subPaint
            }
            canvas.drawText(formatTaka(t.netResult, showSign = true), margin + 450f, y, netPaint)
            y += 18f
        }

        pdf.finishPage(page)
        pdf.writeTo(outputStream)
        pdf.close()
    }

    private fun csvEscape(value: String): String {
        val cleaned = value.replace("\n", " ").replace("\r", " ")
        return if (cleaned.contains(",") || cleaned.contains("\"")) {
            "\"${cleaned.replace("\"", "\"\"")}\""
        } else {
            cleaned
        }
    }
}
