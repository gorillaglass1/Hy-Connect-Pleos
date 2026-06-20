package com.hyconnect.pleos.navigation

import android.content.Context
import android.util.Log
import ai.pleos.playground.navi.constants.WaypointIndex
import ai.pleos.playground.navi.data.RequestWaypointInfo
import ai.pleos.playground.navi.helper.NaviHelper
import com.hyconnect.pleos.data.model.HydrogenStation

/**
 * Pleos NaviHelper SDK를 사용해 충전소를 경유지로 추가한다.
 * 수소충전소 서버 ID를 POI ID로 함께 전달하고 좌표로 경유지를 설정한다.
 *
 * Lifecycle: Activity.onCreate() → initialize(), Activity.onDestroy() → release()
 */
class PleosNaviHelperNavigationClient(
    context: Context,
    private val fallback: NavigationClient = AndroidGeoNavigationClient(context),
) : NavigationClient {
    private val naviHelper = NaviHelper(context)

    fun initialize() = naviHelper.initialize()
    fun release() = naviHelper.release()

    override fun startRouteGuidance(station: HydrogenStation): NavigationResult =
        addWaypointToNavi(station)

    override fun addWaypoint(station: HydrogenStation): NavigationResult =
        addWaypointToNavi(station)

    private fun addWaypointToNavi(station: HydrogenStation): NavigationResult {
        val lat = station.latitude
            ?: return NavigationResult.Failed("${station.name}의 좌표 정보가 없습니다.")
        val lng = station.longitude
            ?: return NavigationResult.Failed("${station.name}의 좌표 정보가 없습니다.")
        val poiId = station.id
            .filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }
            .ifBlank { "hyconnect${station.name.hashCode().toUInt()}" }

        return try {
            naviHelper.addWaypoint(
                RequestWaypointInfo(
                    latitude = lat,
                    longitude = lng,
                    waypointIndex = WaypointIndex.FIRST,
                    poiId = poiId,
                    poiName = station.name,
                    poiSubId = "0",
                    address = station.address,
                ),
            )
            NavigationResult.WaypointAdded(station.name)
        } catch (e: Exception) {
            Log.w("HyConnect", "NaviHelper 경유지 추가 실패, geo 폴백으로 전환", e)
            fallback.addWaypoint(station)
        }
    }
}
