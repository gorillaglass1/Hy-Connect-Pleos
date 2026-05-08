package com.hyconnect.pleos.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HyConnectService {
    @GET("hydrogen-stations")
    suspend fun getHydrogenStations(
        @Query("hydrogen_station_id") hydrogenStationId: Int? = null,
        @Query("name") name: String? = null,
        @Query("address") address: String? = null,
        @Query("payment_supported") paymentSupported: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): List<HydrogenStationDto>

    @GET("hydrogen-station-realtime")
    suspend fun getHydrogenStationRealtime(
        @Query("realtime_id") realtimeId: Int? = null,
        @Query("hydrogen_station_id") hydrogenStationId: Int? = null,
        @Query("station_status") stationStatus: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): List<HydrogenStationRealtimeDto>

    @GET("hydrogen-chargers")
    suspend fun getHydrogenChargers(
        @Query("charger_id") chargerId: Int? = null,
        @Query("hydrogen_charger_id") hydrogenChargerId: Int? = null,
        @Query("station_id") stationId: Int? = null,
        @Query("hydrogen_station_id") hydrogenStationId: Int? = null,
        @Query("charger_status") chargerStatus: String? = null,
        @Query("pressure_type") pressureType: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): List<HydrogenChargerDto>

    @GET("recommendation-histories")
    suspend fun getRecommendationHistories(
        @Query("recommendation_id") recommendationId: Int? = null,
        @Query("user_id") userId: Int? = null,
        @Query("vehicle_id") vehicleId: Int? = null,
        @Query("hydrogen_station_id") hydrogenStationId: Int? = null,
        @Query("selected") selected: Boolean? = null,
        @Query("recommendation_type") recommendationType: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): List<RecommendationHistoryDto>

    @GET("vehicles")
    suspend fun getVehicles(
        @Query("user_id") userId: Int,
        @Query("vehicle_id") vehicleId: Int? = null,
        @Query("vehicle_number") vehicleNumber: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): List<VehicleDto>

    @GET("vehicles/{vehicle_id}")
    suspend fun getVehicle(
        @Path("vehicle_id") vehicleId: Int,
    ): VehicleDto

    @POST("hydrogen-station-reservations")
    suspend fun createReservation(
        @Body request: ReservationRequestDto,
    ): ReservationResponseDto

    @POST("charging-logs")
    suspend fun createChargingLog(
        @Body request: ChargingLogRequestDto,
    ): ChargingLogResponseDto

    @GET("user")
    suspend fun getUser(
        @Query("user_id") userId: Int? = null,
        @Query("email") email: String? = null,
        @Query("phone") phone: String? = null,
    ): UserDto

    @POST("user")
    suspend fun createUser(
        @Body request: UserRequestDto,
    ): UserDto

    @PATCH("user/update/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: Int,
        @Body request: UserRequestDto,
    ): UserDto

    @POST("recommendations/optimized-stations")
    suspend fun getOptimizedStationRecommendations(
        @Body request: OptimizedStationRecommendationRequestDto,
    ): OptimizedStationRecommendationResponseDto
}
