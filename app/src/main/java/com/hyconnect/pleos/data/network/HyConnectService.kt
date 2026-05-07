package com.hyconnect.pleos.data.network

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import retrofit2.http.GET

interface HyConnectService {
    // TODO: FastAPI 배포 서버 문서(/docs)의 실제 경로로 교체한다.
    @GET("vehicle/state")
    suspend fun getVehicleState(): VehicleState

    // TODO: FastAPI 배포 서버 문서(/docs)의 실제 경로와 응답 모델로 교체한다.
    @GET("recommendation")
    suspend fun getAiRecommendation(): AiRecommendation

    // TODO: 위치/인증/차량 ID 파라미터가 확정되면 Query/Header를 추가한다.
    @GET("stations/recommended")
    suspend fun getRecommendedStations(): List<HydrogenStation>
}
