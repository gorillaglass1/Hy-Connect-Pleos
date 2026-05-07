package com.hyconnect.pleos.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hyconnect.pleos.data.model.HydrogenStation

class AndroidGeoNavigationClient(
    private val context: Context,
) : NavigationClient {
    override fun startRouteGuidance(station: HydrogenStation): NavigationResult {
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
            NavigationResult.Started(station.name)
        } catch (exception: ActivityNotFoundException) {
            NavigationResult.Failed("연결 가능한 내비게이션 앱을 찾지 못했습니다.", exception)
        } catch (exception: SecurityException) {
            NavigationResult.Failed("내비게이션 실행 권한이 없습니다.", exception)
        }
    }
}
