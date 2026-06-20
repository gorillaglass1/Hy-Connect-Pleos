package com.hyconnect.pleos.navigation

import android.content.Context
import android.util.Log
import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.RouteDriving
import ai.pleos.playground.navi.constants.WaypointIndex
import ai.pleos.playground.navi.data.RequestWaypointInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteStateInfo
import ai.pleos.playground.navi.data.WaypointChangedInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import com.hyconnect.pleos.data.model.HydrogenStation

/**
 * Pleos NaviHelper SDK로 충전소까지 경로를 시작하거나 경유지로 추가한다.
 *
 * 동작 방식(중요):
 * - [NaviHelper.addWaypoint]/[NaviHelper.requestRoute]는 데이터를 Gson JSON으로 직렬화해 AIDL로 Navi 앱에
 *   전달하는 **비동기 void 호출**이다. 좌표/필드 검증과 경로 탐색은 Navi 앱(호스트)이 수행하며, 결과는
 *   리스너 콜백으로 통지된다.
 * - **`addWaypoint`는 이미 진행 중인 경로가 있어야 동작한다.** 활성 경로가 없는데 경유지를 추가하면
 *   재탐색할 대상(목적지)이 없어 호스트가 "경로 탐색 오류 / invalid parameter"로 거부한다.
 *   → 따라서 현재 경로 상태([isRouting])를 추적해, 경로가 있으면 경유지 추가, 없으면 그 충전소로
 *     **경로를 새로 시작([requestRoute])** 한다.
 * - 잘못된 좌표는 전송 전에 [validate]로 걸러 호스트가 거부할 요청을 애초에 보내지 않는다.
 *
 * Lifecycle: Activity.onCreate() → initialize(), Activity.onDestroy() → release()
 */
class PleosNaviHelperNavigationClient(
    context: Context,
    private val fallback: NavigationClient = AndroidGeoNavigationClient(context),
) : NavigationClient {
    private val naviHelper = NaviHelper(context)

    /** Navi 앱이 비동기로 통지하는 경로 시작/경유지 변경/오류 결과를 호출 측에 전달한다. */
    var onNaviEvent: ((NavigationResult) -> Unit)? = null

    /** 현재 Navi 앱이 경로 안내 중인지. 경유지 추가 가능 여부를 좌우한다. 콜백으로 갱신된다. */
    @Volatile
    private var isRouting: Boolean = false

    private val eventListener = object : NaviHelperEventListener {
        override fun onRouteStarted(routeStartInfo: RouteStartInfo) {
            isRouting = true
        }

        override fun onRouteEnded() {
            isRouting = false
        }

        override fun onRouteCancelled() {
            isRouting = false
        }

        override fun onNavigationStatus(status: RouteDriving) {
            isRouting = status == RouteDriving.START
        }

        override fun onRouteStateInfo(routeStateInfo: RouteStateInfo) {
            // initialize()에서 요청하는 초기 경로 상태 동기화.
            isRouting = routeStateInfo.isRouting
        }

        override fun onWaypointChanged(waypointChangedInfo: WaypointChangedInfo) {
            onNaviEvent?.invoke(NavigationResult.WaypointAdded("경유지"))
        }

        override fun onError(errorCode: NaviErrorCode) {
            Log.w("HyConnect", "NaviHelper onError: $errorCode")
            onNaviEvent?.invoke(NavigationResult.Failed(messageFor(errorCode)))
        }
    }

    fun initialize() {
        naviHelper.initialize()
        naviHelper.addListener(eventListener)
        // 현재 경로 상태를 미리 받아 둔다. (onRouteStateInfo로 통지됨)
        runCatching { naviHelper.getRouteStateInfo() }
    }

    fun release() {
        naviHelper.removeListener(eventListener)
        naviHelper.release()
    }

    /** 충전소를 목적지로 새 경로 안내를 시작한다. */
    override fun startRouteGuidance(station: HydrogenStation): NavigationResult =
        validate(station)?.let { NavigationResult.Failed(it) } ?: requestRouteTo(station)

    /**
     * 경로 안내 중이면 충전소를 경유지로 추가하고, 안내 중이 아니면 그 충전소로 경로를 시작한다.
     * (활성 경로 없이 경유지를 추가하면 호스트가 invalid parameter로 거부하기 때문)
     */
    override fun addWaypoint(station: HydrogenStation): NavigationResult {
        validate(station)?.let { return NavigationResult.Failed(it) }
        return if (isRouting) addWaypointTo(station) else requestRouteTo(station)
    }

    private fun addWaypointTo(station: HydrogenStation): NavigationResult {
        val lat = station.latitude!!
        val lng = station.longitude!!
        return try {
            naviHelper.addWaypoint(
                RequestWaypointInfo(
                    latitude = lat,
                    longitude = lng,
                    waypointIndex = WaypointIndex.FIRST,
                    poiId = station.id.trim(),
                    poiName = station.name.trim(),
                    poiSubId = "",
                    address = station.address.ifBlank { station.name }.trim(),
                ),
            )
            // void 비동기 호출. 전송 성공을 의미하며, 최종 결과는 onNaviEvent로 통지된다.
            NavigationResult.WaypointAdded(station.name)
        } catch (e: Exception) {
            Log.w("HyConnect", "NaviHelper 경유지 추가 실패, geo 폴백으로 전환", e)
            fallback.addWaypoint(station)
        }
    }

    private fun requestRouteTo(station: HydrogenStation): NavigationResult {
        val lat = station.latitude!!
        val lng = station.longitude!!
        return try {
            naviHelper.requestRoute(
                RouteInfo(
                    latitude = lat,
                    longitude = lng,
                    poiId = station.id.trim(),
                    poiName = station.name.trim(),
                    poiSubId = "",
                    address = station.address.ifBlank { station.name }.trim(),
                ),
            )
            NavigationResult.Started(station.name)
        } catch (e: Exception) {
            Log.w("HyConnect", "NaviHelper 경로 시작 실패, geo 폴백으로 전환", e)
            fallback.startRouteGuidance(station)
        }
    }

    /** 잘못된 좌표/필드를 전송 전에 걸러낸다. 문제가 없으면 null을 반환한다. */
    private fun validate(station: HydrogenStation): String? {
        val lat = station.latitude ?: return "${station.name}의 좌표 정보가 없습니다."
        val lng = station.longitude ?: return "${station.name}의 좌표 정보가 없습니다."

        if (lat.isNaN() || lng.isNaN() || lat.isInfinite() || lng.isInfinite()) {
            return "${station.name}의 좌표 값이 올바르지 않습니다."
        }
        if (lat == 0.0 && lng == 0.0) {
            return "${station.name}의 좌표가 설정되지 않았습니다."
        }
        // 국내 서비스 범위. 위/경도가 뒤바뀐 경우(예: lat=127, lng=37)도 여기서 걸러진다.
        if (lat !in KOREA_LAT_RANGE || lng !in KOREA_LNG_RANGE) {
            return "${station.name}의 좌표가 서비스 범위를 벗어났습니다. (위도 $lat, 경도 $lng)"
        }
        if (station.name.isBlank()) {
            return "충전소 이름이 없어 추가할 수 없습니다."
        }
        return null
    }

    private fun messageFor(code: NaviErrorCode): String = when (code) {
        NaviErrorCode.CAUSE_SEARCH_ROUTE,
        NaviErrorCode.CAUSE_SEARCH_ROUTE_OVER_DISTANCE,
        NaviErrorCode.CAUSE_SEARCH_ROUTE_TOO_SHORT_DISTANCE ->
            "경로를 탐색하지 못했습니다. 잠시 후 다시 시도해 주세요."
        NaviErrorCode.CAUSE_LOW_BATTERY -> "차량 배터리가 부족해 경로를 탐색할 수 없습니다."
        else -> "경로/경유지 처리 중 오류가 발생했습니다. ($code)"
    }

    private companion object {
        // 대한민국 영역 대략 범위(부속 도서 포함 여유값).
        val KOREA_LAT_RANGE = 33.0..39.6
        val KOREA_LNG_RANGE = 124.5..132.0
    }
}
