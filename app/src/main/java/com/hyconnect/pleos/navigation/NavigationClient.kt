package com.hyconnect.pleos.navigation

import com.hyconnect.pleos.data.model.HydrogenStation

interface NavigationClient {
    fun startRouteGuidance(station: HydrogenStation): NavigationResult
}

sealed interface NavigationResult {
    data class Started(val stationName: String) : NavigationResult
    data class Failed(val message: String, val cause: Throwable? = null) : NavigationResult
}
