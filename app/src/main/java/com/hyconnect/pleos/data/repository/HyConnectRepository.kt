package com.hyconnect.pleos.data.repository

import android.util.Log
import com.google.gson.JsonParseException
import com.hyconnect.pleos.data.mapper.mergeWithRealtimeAndChargers
import com.hyconnect.pleos.data.mapper.toAiRecommendation
import com.hyconnect.pleos.data.mapper.toRecommendedStations
import com.hyconnect.pleos.data.mapper.toVehicleState
import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.ChargingLogRequestDto
import com.hyconnect.pleos.data.network.ChargingLogResponseDto
import com.hyconnect.pleos.data.network.HydrogenChargerDto
import com.hyconnect.pleos.data.network.HydrogenStationDto
import com.hyconnect.pleos.data.network.HydrogenStationRealtimeDto
import com.hyconnect.pleos.data.network.HyConnectService
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.data.network.OptimizedCandidateStationDto
import com.hyconnect.pleos.data.network.OptimizedChargerCandidateDto
import com.hyconnect.pleos.data.network.OptimizedDestinationDto
import com.hyconnect.pleos.data.network.OptimizedLocationDto
import com.hyconnect.pleos.data.network.OptimizedNavigationContextDto
import com.hyconnect.pleos.data.network.OptimizedRecommendationPreferencesDto
import com.hyconnect.pleos.data.network.OptimizedRecommendationTriggerDto
import com.hyconnect.pleos.data.network.OptimizedRealtimeStationStatusDto
import com.hyconnect.pleos.data.network.OptimizedStationRecommendationRequestDto
import com.hyconnect.pleos.data.network.OptimizedStationRecommendationResponseDto
import com.hyconnect.pleos.data.network.OptimizedVehicleContextDto
import com.hyconnect.pleos.data.network.ReservationRequestDto
import com.hyconnect.pleos.data.network.ReservationResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit

interface HyConnectRepository {
    suspend fun getVehicleState(): NetworkResult<VehicleState>
    suspend fun getAiRecommendation(): NetworkResult<AiRecommendation>
    suspend fun getRecommendedStations(): NetworkResult<List<HydrogenStation>>
    suspend fun createReservation(
        stationId: Int,
        chargerId: Int,
        userId: Int = DEFAULT_USER_ID,
    ): NetworkResult<ReservationResponseDto>

    suspend fun createChargingLog(
        userId: Int = DEFAULT_USER_ID,
        stationId: Int,
        vehicleId: Int = DEFAULT_VEHICLE_ID,
        startTime: String,
        endTime: String,
        chargedAmount: Double? = null,
        chargingCost: Double? = null,
        waitingTime: Int? = null,
    ): NetworkResult<ChargingLogResponseDto>

    companion object {
        const val DEFAULT_USER_ID = 1
        const val DEFAULT_VEHICLE_ID = 1
    }
}

class HyConnectRepositoryImpl(
    private val service: HyConnectService,
    private val userId: Int = HyConnectRepository.DEFAULT_USER_ID,
    private val vehicleId: Int = HyConnectRepository.DEFAULT_VEHICLE_ID,
) : HyConnectRepository {
    // TODO: 추후 Pleos Fused Location SDK 또는 Android Location API로 교체.
    private val currentLat: Double? = 37.5665
    private val currentLng: Double? = 126.9780

    override suspend fun getVehicleState(): NetworkResult<VehicleState> = safeApiCall {
        val vehicle = loadVehicleOrNull()
        vehicle?.toVehicleState() ?: VehicleState(
            hydrogenPercent = 28,
            vehicleRangeKm = 96,
            message = "로컬 추천 서버 테스트를 위해 임시 차량 상태를 사용합니다.",
        )
    }

    override suspend fun getAiRecommendation(): NetworkResult<AiRecommendation> = safeApiCall {
        runCatching {
            loadOptimizedStationRecommendation().toAiRecommendation()
        }.getOrElse {
            loadAiRecommendationFromHistories()
        }
    }

    override suspend fun getRecommendedStations(): NetworkResult<List<HydrogenStation>> = safeApiCall {
        runCatching {
            loadOptimizedStationRecommendation().toRecommendedStations()
        }.getOrElse {
            runCatching {
                loadRecommendedStationsFromFastApiResources()
            }.getOrElse {
                // uvicorn 서버가 꺼져 있어도 Pleos 에뮬레이터 화면 검증은 가능하도록 최소 데모 후보를 표시한다.
                demoOptimizedCandidateStations().mapIndexed { index, station ->
                    station.toFallbackHydrogenStation(isRecommended = index == 0)
                }
            }
        }
    }

    private suspend fun loadAiRecommendationFromHistories(): AiRecommendation {
        val histories = runCatching {
            service.getRecommendationHistories(userId = userId, vehicleId = vehicleId)
        }.getOrDefault(emptyList())
        val bestHistory = histories.maxByOrNull { it.recommendationScore ?: Double.NEGATIVE_INFINITY }

        return if (bestHistory == null) {
            AiRecommendation(
                title = "지금 충전하기 좋은 타이밍이에요",
                dustSummary = "",
                routeSummary = "",
                reason = "실시간 충전소 상태와 대기 시간을 기준으로 추천합니다.",
            )
        } else {
            val stationName = runCatching {
                service.getHydrogenStations(hydrogenStationId = bestHistory.hydrogenStationId, limit = 1)
                    .firstOrNull()
                    ?.name
            }.getOrNull()
            bestHistory.toAiRecommendation(stationName)
        }
    }

    private suspend fun loadRecommendedStationsFromFastApiResources(): List<HydrogenStation> {
        val stations = service.getHydrogenStations()
        val realtime = service.getHydrogenStationRealtime()
        val chargers = service.getHydrogenChargers()
        val histories = runCatching {
            service.getRecommendationHistories(userId = userId, vehicleId = vehicleId)
        }.getOrDefault(emptyList())

        return stations.mergeWithRealtimeAndChargers(
            realtimeList = realtime,
            chargerList = chargers,
            recommendationHistories = histories,
            currentLat = currentLat,
            currentLng = currentLng,
        )
    }

    private suspend fun loadOptimizedStationRecommendation(): OptimizedStationRecommendationResponseDto {
        val vehicle = loadVehicleOrNull()
        val vehicleState = vehicle?.toVehicleState() ?: VehicleState(
            hydrogenPercent = 28,
            vehicleRangeKm = 96,
            message = "로컬 추천 서버 테스트를 위해 임시 차량 상태를 사용합니다.",
        )
        val candidateStations = withTimeoutOrNull(1_500) {
            val stations = service.getHydrogenStations()
            val realtime = service.getHydrogenStationRealtime()
            val chargers = service.getHydrogenChargers()
            stations.toOptimizedCandidates(
                realtimeList = realtime,
                chargerList = chargers,
            )
        } ?: demoOptimizedCandidateStations()

        return service.getOptimizedStationRecommendations(
            OptimizedStationRecommendationRequestDto(
                userId = userId,
                vehicle = OptimizedVehicleContextDto(
                    vehicleId = vehicle?.vehicleId ?: vehicleId,
                    model = vehicle?.model ?: "NEXO",
                    fuelType = vehicle?.fuelType ?: "hydrogen",
                    remainingHydrogenPercent = vehicleState.hydrogenPercent,
                    remainingRangeKm = vehicleState.vehicleRangeKm,
                    tankCapacityKg = vehicle?.tankCapacity ?: 6.33,
                    avgEfficiencyKmPerKg = vehicle?.avgEfficiency ?: 96.0,
                ),
                location = OptimizedLocationDto(
                    latitude = currentLat ?: 0.0,
                    longitude = currentLng ?: 0.0,
                    timestamp = Instant.now().toString(),
                ),
                // TODO: Pleos NaviHelper SDK에서 목적지, 경로 거리, 도착 예상 잔여 주행가능거리, route polyline을 받아 교체.
                navigation = OptimizedNavigationContextDto(
                    destination = OptimizedDestinationDto(
                        name = "부산역",
                        latitude = 35.1151,
                        longitude = 129.0415,
                    ),
                    remainingRouteDistanceKm = 390.5,
                    estimatedArrivalTime = null,
                    estimatedRemainingRangeAtArrivalKm = 12,
                    routePolyline = null,
                ),
                trigger = OptimizedRecommendationTriggerDto(
                    type = if (vehicleState.hydrogenPercent <= 30) "LOW_FUEL" else "LOW_RANGE",
                    reason = "주행가능거리 또는 도착 예상 잔여 주행가능거리가 임계값 이하입니다.",
                    rangeThresholdKm = 120,
                    arrivalRangeThresholdKm = 50,
                    fuelThresholdPercent = 30,
                ),
                preferences = OptimizedRecommendationPreferencesDto(
                    prefer700bar = true,
                    maxDetourKm = 15.0,
                    prioritize = listOf(
                        "reachable",
                        "wait_time",
                        "price",
                        "detour_distance",
                        "charger_status",
                    ),
                ),
                candidateStations = candidateStations,
            ),
        )
    }

    private suspend fun loadVehicleOrNull() = withTimeoutOrNull(1_500) {
        runCatching { service.getVehicle(vehicleId) }.recoverCatching {
            service.getVehicles(userId = userId, vehicleId = vehicleId).first()
        }.getOrNull()
    }

    // TODO: 향후 예약 버튼 추가 시 이 Repository 함수를 연결.
    override suspend fun createReservation(
        stationId: Int,
        chargerId: Int,
        userId: Int,
    ): NetworkResult<ReservationResponseDto> = safeApiCall {
        val reservationTime = Instant.now()
        val expireTime = reservationTime.plus(10, ChronoUnit.MINUTES)
        service.createReservation(
            ReservationRequestDto(
                hydrogenChargerId = chargerId,
                hydrogenStationId = stationId,
                reservationStatus = "reserved",
                userId = userId,
                reservationTime = reservationTime.toString(),
                expireTime = expireTime.toString(),
            ),
        )
    }

    // TODO: 충전 완료 이벤트가 생기면 Pleos Vehicle SDK 또는 서버 이벤트와 연결.
    override suspend fun createChargingLog(
        userId: Int,
        stationId: Int,
        vehicleId: Int,
        startTime: String,
        endTime: String,
        chargedAmount: Double?,
        chargingCost: Double?,
        waitingTime: Int?,
    ): NetworkResult<ChargingLogResponseDto> = safeApiCall {
        service.createChargingLog(
            ChargingLogRequestDto(
                userId = userId,
                hydrogenStationId = stationId,
                vehicleId = vehicleId,
                startTime = startTime,
                endTime = endTime,
                chargedAmount = chargedAmount,
                chargingCost = chargingCost,
                waitingTime = waitingTime,
            ),
        )
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> =
        withContext(Dispatchers.IO) {
            try {
                NetworkResult.Success(block())
            } catch (exception: IOException) {
                Log.w("HyConnect", "network request failed", exception)
                NetworkResult.Error("서버에 연결할 수 없습니다.", exception)
            } catch (exception: HttpException) {
                val message = when (exception.code()) {
                    404 -> "요청한 데이터를 찾을 수 없습니다."
                    in 500..599 -> "서버 오류가 발생했습니다."
                    else -> "데이터를 불러오지 못했습니다."
                }
                Log.w("HyConnect", "http request failed code=${exception.code()}", exception)
                NetworkResult.Error(message, exception)
            } catch (exception: JsonParseException) {
                Log.w("HyConnect", "json parsing failed", exception)
                NetworkResult.Error("서버 응답 형식이 올바르지 않습니다.", exception)
            } catch (exception: IllegalStateException) {
                Log.w("HyConnect", "response mapping failed", exception)
                NetworkResult.Error("서버 응답 형식이 올바르지 않습니다.", exception)
            } catch (exception: Exception) {
                Log.w("HyConnect", "unknown request failed", exception)
                NetworkResult.Error("데이터를 불러오지 못했습니다.", exception)
            }
        }
}

private fun demoOptimizedCandidateStations(): List<OptimizedCandidateStationDto> =
    listOf(
        OptimizedCandidateStationDto(
            hydrogenStationId = 101,
            name = "양재 수소충전소",
            address = "서울 서초구",
            latitude = 37.4681,
            longitude = 127.0387,
            distanceFromCurrentKm = 8.4,
            detourDistanceKm = 2.1,
            isOnRoute = true,
            pricePerKg = 9900,
            paymentSupported = "card",
            realtime = OptimizedRealtimeStationStatusDto(
                availableChargers = 1,
                inUseChargers = 1,
                queueCount = 2,
                avgWaitTime = 10,
                hydrogenStockKg = 120.5,
                stationStatus = "OPEN",
                updatedAt = Instant.now().toString(),
            ),
            chargers = listOf(
                OptimizedChargerCandidateDto(
                    hydrogenChargerId = 1001,
                    chargerStatus = "AVAILABLE",
                    hydrogenPressureBar = 700,
                    pressureType = "700bar",
                ),
            ),
        ),
    )

private fun OptimizedCandidateStationDto.toFallbackHydrogenStation(isRecommended: Boolean): HydrogenStation =
    HydrogenStation(
        id = hydrogenStationId.toString(),
        name = name,
        address = address,
        status = realtime?.stationStatus ?: "서버 연결 대기",
        pressureInfo = if (chargers.any { it.hydrogenPressureBar == 700 || it.pressureType?.contains("700") == true }) {
            "700bar 사용 가능"
        } else {
            "압력 정보 없음"
        },
        distanceKm = distanceFromCurrentKm ?: detourDistanceKm ?: 0.0,
        waitMinutes = realtime?.avgWaitTime ?: ((realtime?.queueCount ?: 0) * 5),
        isRecommended = isRecommended,
        latitude = latitude,
        longitude = longitude,
    )

private fun List<HydrogenStationDto>.toOptimizedCandidates(
    realtimeList: List<HydrogenStationRealtimeDto>,
    chargerList: List<HydrogenChargerDto>,
): List<OptimizedCandidateStationDto> {
    val realtimeByStationId = realtimeList.associateBy { it.hydrogenStationId }
    val chargersByStationId = chargerList.groupBy { it.hydrogenStationId }

    return map { station ->
        val realtime = realtimeByStationId[station.hydrogenStationId]
        OptimizedCandidateStationDto(
            hydrogenStationId = station.hydrogenStationId,
            name = station.name,
            address = station.address,
            latitude = station.latitude,
            longitude = station.longitude,
            // TODO: NaviHelper 경로 polyline 기반으로 현재 위치 거리와 경로 이탈 거리를 계산해 교체.
            distanceFromCurrentKm = null,
            detourDistanceKm = null,
            isOnRoute = false,
            // TODO: 서버 충전소 가격 필드가 확정되면 price_per_kg를 채운다.
            pricePerKg = null,
            paymentSupported = station.paymentSupported,
            realtime = realtime?.let {
                OptimizedRealtimeStationStatusDto(
                    availableChargers = it.availableChargers,
                    inUseChargers = it.inUseChargers,
                    queueCount = it.queueCount,
                    avgWaitTime = it.avgWaitTime,
                    hydrogenStockKg = it.hydrogenStockKg,
                    stationStatus = it.stationStatus,
                    updatedAt = it.updatedAt,
                )
            },
            chargers = chargersByStationId[station.hydrogenStationId].orEmpty().map { charger ->
                OptimizedChargerCandidateDto(
                    hydrogenChargerId = charger.hydrogenChargerId,
                    chargerStatus = charger.chargerStatus,
                    hydrogenPressureBar = charger.hydrogenPressureBar,
                    pressureType = charger.pressureType,
                )
            },
        )
    }
}
