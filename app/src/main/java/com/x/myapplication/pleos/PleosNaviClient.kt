package com.x.myapplication.pleos

import ai.pleos.playground.navi.constants.WaypointIndex
import ai.pleos.playground.navi.data.RequestWaypointInfo
import ai.pleos.playground.navi.helper.NaviHelper
import android.content.Context
import com.x.myapplication.data.model.RecommendationCard

class PleosNaviClient(context: Context) {
    private val naviHelper = NaviHelper(context.applicationContext)

    fun initialize() {
        naviHelper.initialize()
    }

    fun addWaypoint(card: RecommendationCard): Result<Unit> = runCatching {
        val latitude = card.latitude ?: error("충전소 위도 정보가 없습니다.")
        val longitude = card.longitude ?: error("충전소 경도 정보가 없습니다.")

        naviHelper.addWaypoint(
            RequestWaypointInfo(
                longitude = longitude,
                latitude = latitude,
                poiName = card.stationName,
                waypointIndex = WaypointIndex.FIRST,
                poiId = card.poiId.orEmpty(),
                address = card.address.orEmpty(),
                poiSubId = card.poiSubId ?: "0",
            )
        )
    }

    fun release() {
        naviHelper.release()
    }
}
