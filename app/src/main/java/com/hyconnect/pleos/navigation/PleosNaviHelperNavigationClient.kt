package com.hyconnect.pleos.navigation

import com.hyconnect.pleos.data.model.HydrogenStation

/**
 * Pleos NaviHelper SDK 기반 구현 자리(스캐폴드).
 *
 * 문서: https://document.pleos.ai/api-reference/connect-sdk-pleos/NaviHelper/intro
 * 의존성: ai.pleos.playground:NaviHelper:2.0.3
 * 권한:   pleos.car.permission.NAVI_ROUTE, pleos.car.permission.NAVI_ROUTE_SEARCH (AndroidManifest에 선언됨)
 *
 * 승인된 SDK가 추가되면 [fallback] 위임을 걷어내고 아래 형태로 교체한다.
 *
 *   private val naviHelper = NaviHelper.getInstance(context)
 *   fun initialize() = naviHelper.initialize()           // onCreate
 *   fun release()    = naviHelper.release()               // onDestroy
 *
 *   override fun startRouteGuidance(station): NavigationResult {
 *       naviHelper.requestRoute(Destination(station.latitude, station.longitude, station.name))
 *   }
 *   override fun addWaypoint(station): NavigationResult {
 *       naviHelper.addWaypoint(Waypoint(station.latitude, station.longitude, station.name))
 *   }
 *
 * 현재는 SDK 미승인 상태이므로 geo:/Pleos 지도 폴백([AndroidGeoNavigationClient])에 위임한다.
 */
class PleosNaviHelperNavigationClient(
    private val fallback: NavigationClient,
) : NavigationClient {
    override fun startRouteGuidance(station: HydrogenStation): NavigationResult =
        fallback.startRouteGuidance(station)

    override fun addWaypoint(station: HydrogenStation): NavigationResult =
        fallback.addWaypoint(station)
}
