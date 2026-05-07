package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.HyConnectService
import com.hyconnect.pleos.data.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface HyConnectRepository {
    suspend fun getVehicleState(): NetworkResult<VehicleState>
    suspend fun getAiRecommendation(): NetworkResult<AiRecommendation>
    suspend fun getRecommendedStations(): NetworkResult<List<HydrogenStation>>
}

class HyConnectRepositoryImpl(
    private val service: HyConnectService,
) : HyConnectRepository {
    override suspend fun getVehicleState(): NetworkResult<VehicleState> = safeApiCall {
        service.getVehicleState()
    }

    override suspend fun getAiRecommendation(): NetworkResult<AiRecommendation> = safeApiCall {
        service.getAiRecommendation()
    }

    override suspend fun getRecommendedStations(): NetworkResult<List<HydrogenStation>> = safeApiCall {
        service.getRecommendedStations()
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> =
        withContext(Dispatchers.IO) {
            try {
                NetworkResult.Success(block())
            } catch (exception: IOException) {
                // TODO: 네트워크 오류, 서버 오류, 인증 오류를 사용자 액션별 메시지로 세분화한다.
                NetworkResult.Error("서버에 연결할 수 없습니다.", exception)
            } catch (exception: Exception) {
                // TODO: FastAPI 오류 응답 바디를 파싱해 구체적인 실패 사유를 표시한다.
                NetworkResult.Error("데이터를 불러오지 못했습니다.", exception)
            }
        }
}
