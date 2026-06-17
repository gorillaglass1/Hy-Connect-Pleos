package com.hyconnect.pleos.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.app.SearchManager
import com.hyconnect.pleos.data.model.HydrogenStation

/**
 * NaviHelper SDK 승인 전까지 쓰는 폴백 구현.
 * geo: 인텐트 또는 Pleos 지도 검색으로 충전소 위치를 띄운다.
 * (geo: 인텐트는 실제 '경유지 추가'를 지원하지 않으므로, NaviHelper SDK가 붙으면
 *  [PleosNaviHelperNavigationClient]가 addWaypoint(경위도)로 교체한다.)
 */
class AndroidGeoNavigationClient(
    private val context: Context,
) : NavigationClient {
    override fun startRouteGuidance(station: HydrogenStation): NavigationResult =
        launchForStation(station) { NavigationResult.Started(station.name) }

    override fun addWaypoint(station: HydrogenStation): NavigationResult =
        launchForStation(station) { NavigationResult.WaypointAdded(station.name) }

    private inline fun launchForStation(
        station: HydrogenStation,
        onSuccess: () -> NavigationResult,
    ): NavigationResult {
        val latitude = station.latitude
        val longitude = station.longitude

        if (latitude == null || longitude == null) {
            return NavigationResult.Failed("${station.name}의 좌표 정보가 없습니다.")
        }

        val encodedName = Uri.encode(station.name)
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedName)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            onSuccess()
        } catch (exception: ActivityNotFoundException) {
            startPleosMapSearch(station, onSuccess)
        } catch (exception: SecurityException) {
            NavigationResult.Failed("내비게이션 실행 권한이 없습니다.", exception)
        }
    }

    private inline fun startPleosMapSearch(
        station: HydrogenStation,
        onSuccess: () -> NavigationResult,
    ): NavigationResult {
        val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
            setPackage(PLEOS_MAP_PACKAGE)
            putExtra(SearchManager.QUERY, station.name)
            putExtra("latitude", station.latitude)
            putExtra("longitude", station.longitude)
            putExtra("name", station.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(searchIntent)
            onSuccess()
        } catch (exception: ActivityNotFoundException) {
            NavigationResult.Failed("연결 가능한 내비게이션 앱을 찾지 못했습니다.", exception)
        } catch (exception: SecurityException) {
            NavigationResult.Failed("내비게이션 실행 권한이 없습니다.", exception)
        }
    }

    private companion object {
        const val PLEOS_MAP_PACKAGE = "ai.umos.maps.android.navigation.app"
    }
}
