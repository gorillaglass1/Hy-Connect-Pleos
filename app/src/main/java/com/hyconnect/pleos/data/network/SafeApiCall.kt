package com.hyconnect.pleos.data.network

import android.util.Log
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Retrofit 호출을 감싸 예외를 [NetworkResult]로 변환하는 공용 헬퍼.
 * 여러 Repository(차량/충전소, 날씨 등)가 동일한 예외 처리/로그 정책을 공유한다.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> =
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
