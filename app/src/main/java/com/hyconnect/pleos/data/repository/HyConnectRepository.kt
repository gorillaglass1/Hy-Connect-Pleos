package com.hyconnect.pleos.data.repository

import android.util.Log
import com.google.gson.JsonParseException
import com.hyconnect.pleos.data.mapper.toStationRecommendationFromDelivery
import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.StationRecommendation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.ChargingLogRequestDto
import com.hyconnect.pleos.data.network.ChargingLogResponseDto
import com.hyconnect.pleos.data.network.DeliveryStationDto
import com.hyconnect.pleos.data.network.HyConnectService
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.data.network.PersonalizedRecommendationRequestDto
import com.hyconnect.pleos.data.network.PreferenceLearningRequestDto
import com.hyconnect.pleos.data.network.UserPreferenceResponseDto
import com.hyconnect.pleos.location.CurrentLocationStore
import com.hyconnect.pleos.location.DestinationStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

interface HyConnectRepository {
    suspend fun getVehicleState(): NetworkResult<VehicleState>
    suspend fun getAiRecommendation(): NetworkResult<AiRecommendation>
    suspend fun getRecommendedStations(): NetworkResult<List<HydrogenStation>>

    /**
     * 주행가능거리가 임계값 이하일 때 자연어 질의로 충전소 추천을 받는다.
     * 현재 위치/목적지 좌표는 Repository가 보유한 위치/내비 정보로 채운다.
     *
     * [userId]가 null이면 유저 정보 없이(요청 본문에서 user_id 생략) 추천을 요청한다.
     */
    suspend fun getNlRecommendedStations(
        nlQuery: String,
        remainingRange: Int,
        userId: Int? = null,
    ): NetworkResult<StationRecommendation>

    /**
     * 추천 카드의 경로안내(선택) 시 호출. 선택한 충전소 관리번호로 선호 가중치를 학습시킨다.
     */
    suspend fun submitStationSelection(
        chrstnMno: String,
        userId: Int = DEFAULT_USER_ID,
    ): NetworkResult<UserPreferenceResponseDto>

    suspend fun createChargingLog(
        chrstnMno: String,
        startTime: String,
        endTime: String,
        userId: Int = DEFAULT_USER_ID,
        chargedAmount: Double? = null,
        chargingCost: Double? = null,
        waitingTime: Int? = null,
    ): NetworkResult<ChargingLogResponseDto>

    companion object {
        const val DEFAULT_USER_ID = 1
    }
}

class HyConnectRepositoryImpl(
    private val service: HyConnectService,
    // 로그인 사용자가 없으면 null. 이 경우 추천 요청 본문에서 user_id를 비워서 보낸다.
    private val userId: Int? = null,
) : HyConnectRepository {
    // 현재 위치는 NaviHelper onCurrentLocationInfo → CurrentLocationStore로 갱신된다.
    // 아직 한 번도 수신하지 못했을 때만 아래 기본 좌표로 폴백한다.
    private val defaultLat: Double = 37.405
    private val defaultLng: Double = 126.721

    // 목적지는 NaviHelper onDestinationInfo → DestinationStore로 갱신된다.
    // 경로 안내 중이 아니면 비어 있으며(null), 이때는 목적지 없이 요청한다.

    override suspend fun getVehicleState(): NetworkResult<VehicleState> = safeApiCall {
        // 서버에는 차량 테이블이 없다. 연료/주행가능거리는 클라이언트 입력값이다.
        // TODO: Pleos Vehicle SDK가 연동되면 실제 차량 상태로 교체한다.
        VehicleState(
            hydrogenPercent = 28,
            vehicleRangeKm = 96,
            message = "차량 SDK 연동 전까지 임시 차량 상태를 사용합니다.",
        )
    }

    override suspend fun getAiRecommendation(): NetworkResult<AiRecommendation> = safeApiCall {
        runCatching {
            val top = fetchDeliveryStations(nlQuery = null, remainingRange = DEFAULT_RANGE_KM)
                .firstOrNull { it.isRecommended }
                ?: fetchDeliveryStations(nlQuery = null, remainingRange = DEFAULT_RANGE_KM).firstOrNull()
            AiRecommendation(
                title = top?.let { "${it.name} 방문을 추천해요" } ?: "지금 충전하기 좋은 타이밍이에요",
                dustSummary = "",
                routeSummary = top?.let { "대기 약 ${it.waitMinutes ?: 0}분 · ${it.distanceKm ?: 0.0}km" }.orEmpty(),
                reason = "대기 시간, 거리, 가격, 편의시설을 종합해 추천합니다.",
            )
        }.getOrElse {
            Log.w("HyConnect", "ai recommendation failed, fallback to demo", it)
            demoAiRecommendation()
        }
    }

    override suspend fun getRecommendedStations(): NetworkResult<List<HydrogenStation>> = safeApiCall {
        runCatching {
            fetchDeliveryStations(nlQuery = null, remainingRange = DEFAULT_RANGE_KM)
                .toStationRecommendationFromDelivery()
                .stations
        }.getOrElse {
            Log.w("HyConnect", "recommended stations failed, fallback to demo", it)
            demoNlStationRecommendation(DEFAULT_RANGE_KM.toInt()).stations
        }
    }

    override suspend fun getNlRecommendedStations(
        nlQuery: String,
        remainingRange: Int,
        userId: Int?,
    ): NetworkResult<StationRecommendation> = safeApiCall {
        runCatching {
            fetchDeliveryStations(
                nlQuery = nlQuery,
                remainingRange = remainingRange.toDouble(),
                userId = userId,
            ).toStationRecommendationFromDelivery()
        }.getOrElse {
            Log.w("HyConnect", "nl recommendation failed, fallback to demo", it)
            // 추천 서버가 꺼져 있어도 Pleos 에뮬레이터 화면 검증이 가능하도록 데모 추천을 제공한다.
            demoNlStationRecommendation(remainingRange)
        }
    }

    override suspend fun submitStationSelection(
        chrstnMno: String,
        userId: Int,
    ): NetworkResult<UserPreferenceResponseDto> = safeApiCall {
        service.learnFromSelection(userId, PreferenceLearningRequestDto(chrstnMno = chrstnMno))
    }

    // TODO: 충전 완료 이벤트가 생기면 Pleos Vehicle SDK 또는 서버 이벤트와 연결.
    override suspend fun createChargingLog(
        chrstnMno: String,
        startTime: String,
        endTime: String,
        userId: Int,
        chargedAmount: Double?,
        chargingCost: Double?,
        waitingTime: Int?,
    ): NetworkResult<ChargingLogResponseDto> = safeApiCall {
        service.createChargingLog(
            ChargingLogRequestDto(
                userId = userId,
                chrstnMno = chrstnMno,
                startTime = startTime,
                endTime = endTime,
                chargedAmount = chargedAmount,
                chargingCost = chargingCost,
                waitingTime = waitingTime,
            ),
        ).first()
    }

    /**
     * 로컬 서버의 `recommendations/personalized/delivery-payloads`를 호출해 충전소 목록을 가져온다.
     * 현재 위치는 NaviHelper가 채운 [CurrentLocationStore] 값을 우선 사용하고, 없으면 기본 좌표로 폴백한다.
     */
    private suspend fun fetchDeliveryStations(
        nlQuery: String?,
        remainingRange: Double,
        userId: Int? = this.userId,
    ): List<DeliveryStationDto> {
        val location = CurrentLocationStore.snapshot()
        val destination = DestinationStore.snapshot()
        return service.getRecommendationDeliveryPayloads(
            PersonalizedRecommendationRequestDto(
                userId = userId,
                currentLatitude = location?.latitude ?: defaultLat,
                currentLongitude = location?.longitude ?: defaultLng,
                // 목적지가 없으면 두 값 모두 null → 본문에서 생략되어 현재 위치 근처 추천을 받는다.
                destinationLatitude = destination?.latitude,
                destinationLongitude = destination?.longitude,
                remainingRange = remainingRange,
                nlQuery = nlQuery,
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

    private companion object {
        // 추천 흐름에서 주행가능거리 입력이 없을 때 쓰는 기본값(km).
        const val DEFAULT_RANGE_KM = 100.0
    }
}

private fun demoAiRecommendation(): AiRecommendation =
    AiRecommendation(
        title = "현대 수소충전소 양재 방문을 추천해요",
        dustSummary = "",
        routeSummary = "경로에서 가장 가까운 충전소를 골라봤어요.",
        reason = "대기 시간, 거리, 가격, 편의시설을 종합해 추천합니다.",
    )

private fun demoNlStationRecommendation(remainingRange: Int): StationRecommendation =
    StationRecommendation(
        driverMessage = "주행가능거리 ${remainingRange}km 남았어요. 경로에서 가장 가까운 충전소를 골라봤어요.",
        stations = listOf(
            HydrogenStation(
                id = "demo-nl-1",
                name = "현대 수소충전소 양재",
                address = "서울 서초구 바우뫼로 12길 73",
                status = "도달 가능",
                pressureInfo = "대기실 · 편의점 · 화장실",
                distanceKm = 3.2,
                waitMinutes = 5,
                isRecommended = true,
                latitude = 37.468164,
                longitude = 127.038703,
            ),
            HydrogenStation(
                id = "demo-nl-2",
                name = "H 강동 수소스테이션",
                address = "서울 강동구 천호대로 1452",
                status = "도달 가능",
                pressureInfo = "세차장 · 화장실",
                distanceKm = 8.7,
                waitMinutes = 12,
                latitude = 37.545762,
                longitude = 127.170278,
            ),
            HydrogenStation(
                id = "demo-nl-3",
                name = "남양주 수소충전소",
                address = "경기 남양주시 경춘로 100",
                status = "도달 가능",
                pressureInfo = "편의점",
                distanceKm = 15.4,
                waitMinutes = 9,
                latitude = 37.635120,
                longitude = 127.216540,
            ),
        ),
    )
