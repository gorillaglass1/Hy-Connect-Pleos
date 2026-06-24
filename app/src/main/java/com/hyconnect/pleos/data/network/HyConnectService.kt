package com.hyconnect.pleos.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Hi-connect FastAPI 서버 계약과 1:1 대응하는 Retrofit 인터페이스.
 * 모든 경로는 [BuildConfig.HYCONNECT_BASE_URL] 기준 상대 경로다.
 */
interface HyConnectService {
    @POST("recommendations/personalized")
    suspend fun getPersonalizedRecommendations(
        @Body request: PersonalizedRecommendationRequestDto,
    ): List<RecommendedStationResponseDto>

    @POST("recommendations/personalized/delivery-payloads")
    suspend fun getRecommendationDeliveryPayloads(
        @Body request: PersonalizedRecommendationRequestDto,
    ): List<DeliveryStationDto>

    /** 연료 충분 화면(battery_sufficient)의 서버 드리븐 UI 페이로드를 가져온다. */
    @POST("dashboard/sufficient")
    suspend fun getSufficientDashboard(
        @Body request: PersonalizedRecommendationRequestDto,
    ): SufficientDashboardDto

    @POST("users/{user_id}/preferences/learn")
    suspend fun learnFromSelection(
        @Path("user_id") userId: Int,
        @Body request: PreferenceLearningRequestDto,
    ): UserPreferenceResponseDto

    @GET("users/{user_id}/preferences")
    suspend fun getUserPreferences(@Path("user_id") userId: Int): UserPreferenceResponseDto

    @GET("hydrogen-stations")
    suspend fun getHydrogenStations(
        @Query("chrstn_mno") chrstnMno: String? = null,
        @Query("chrstn_nm") chrstnNm: String? = null,
        @Query("oper_yn") operYn: String? = null,
        @Query("rltm_info_yn") rltmInfoYn: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): List<HydrogenStationDto>

    @POST("charging-logs")
    suspend fun createChargingLog(@Body request: ChargingLogRequestDto): List<ChargingLogResponseDto>
}
