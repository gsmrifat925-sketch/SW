package com.example

import com.example.data.export.ExportBackupManager
import com.example.data.local.TournamentEntity
import com.example.data.model.GameMode
import com.example.data.model.MatchResult
import com.example.data.repository.ShockwaveAnalytics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShockwaveAccountingTest {

    @Test
    fun `team amounts are never multiplied by player count`() {
        val squadMatch = TournamentEntity(
            name = "Squad Clash",
            dateMillis = 1_700_000_000_000L,
            gameMode = GameMode.SQUAD,
            entryFee = 800.0,
            additionalExpenses = 200.0,
            totalWinning = 2500.0,
            matchResult = MatchResult.WIN
        )
        assertEquals(1000.0, squadMatch.totalInvestment, 0.001)
        assertEquals(1500.0, squadMatch.netResult, 0.001)
        assertTrue(squadMatch.isProfit)
    }

    @Test
    fun `dashboard summary separates profit loss and break even accurately`() {
        val matches = listOf(
            TournamentEntity(
                id = 1,
                name = "Win Match",
                dateMillis = 1000L,
                gameMode = GameMode.SQUAD,
                entryFee = 800.0,
                additionalExpenses = 200.0,
                totalWinning = 2500.0,
                matchResult = MatchResult.WIN
            ),
            TournamentEntity(
                id = 2,
                name = "Loss Match",
                dateMillis = 2000L,
                gameMode = GameMode.DUO,
                entryFee = 500.0,
                additionalExpenses = 100.0,
                totalWinning = 200.0,
                matchResult = MatchResult.LOSS
            ),
            TournamentEntity(
                id = 3,
                name = "Even Match",
                dateMillis = 3000L,
                gameMode = GameMode.SOLO,
                entryFee = 300.0,
                additionalExpenses = 0.0,
                totalWinning = 300.0,
                matchResult = MatchResult.BREAK_EVEN
            )
        )
        val summary = ShockwaveAnalytics.computeDashboardSummary(matches)
        assertEquals(1900.0, summary.totalInvestment, 0.01)
        assertEquals(3000.0, summary.totalWinning, 0.01)
        assertEquals(1500.0, summary.totalProfit, 0.01)
        assertEquals(400.0, summary.totalLoss, 0.01)
        assertEquals(1100.0, summary.netPnL, 0.01)
        assertEquals(3, summary.totalMatches)
        assertEquals(1, summary.winningMatches)
        assertEquals(1, summary.losingMatches)
        assertEquals(1, summary.breakEvenMatches)
    }

    @Test
    fun `json backup roundtrip preserves tournaments and starting balance`() {
        val list = listOf(
            TournamentEntity(
                id = 7,
                name = "Dhaka Cup",
                dateMillis = 1_700_000_000_000L,
                gameMode = GameMode.CUSTOM,
                entryFee = 500.0,
                additionalExpenses = 50.0,
                totalWinning = 1200.0,
                matchResult = MatchResult.WIN,
                notes = "Clean sweep"
            )
        )
        val json = ExportBackupManager.exportJsonBackup(
            tournaments = list,
            transactions = emptyList(),
            startingBalance = 2500.0,
            teamName = "SHOCKWAVE",
            teamTagline = "Play • Win • Grow"
        )
        val restored = ExportBackupManager.parseJsonBackup(json)
        assertEquals(2500.0, restored.startingBalance, 0.01)
        assertEquals(1, restored.tournaments.size)
        assertEquals("Dhaka Cup", restored.tournaments.first().name)
        assertEquals(650.0, restored.tournaments.first().netResult, 0.01)
    }
}
