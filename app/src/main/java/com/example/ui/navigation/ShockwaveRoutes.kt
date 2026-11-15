package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface ShockwaveRoute {
    @Serializable
    data object Home : ShockwaveRoute

    @Serializable
    data object Reports : ShockwaveRoute

    @Serializable
    data object History : ShockwaveRoute

    @Serializable
    data object Wallet : ShockwaveRoute

    @Serializable
    data object Settings : ShockwaveRoute

    @Serializable
    data class AddEditTournament(val tournamentId: Long = 0L) : ShockwaveRoute
}
