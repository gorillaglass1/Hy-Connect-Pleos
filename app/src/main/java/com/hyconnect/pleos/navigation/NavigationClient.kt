package com.hyconnect.pleos.navigation

import com.hyconnect.pleos.data.model.HydrogenStation

interface NavigationClient {
    /** 충전소를 목적지로 새 경로 안내를 시작한다. */
    fun startRouteGuidance(station: HydrogenStation): NavigationResult

    /**
     * 현재 진행 중인 경로에 충전소를 경유지로 추가한다.
     * Pleos NaviHelper SDK의 addWaypoint(경위도) API에 대응한다.
     */
    fun addWaypoint(station: HydrogenStation): NavigationResult
}

sealed interface NavigationResult {
    data class Started(val stationName: String) : NavigationResult
    data class WaypointAdded(val stationName: String) : NavigationResult
    data class Failed(val message: String, val cause: Throwable? = null) : NavigationResult
}
