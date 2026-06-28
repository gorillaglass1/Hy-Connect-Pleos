package com.hyconnect.pleos.data.network

import com.hyconnect.pleos.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        // 실서버(Cloud Run 콜드스타트/Gemini 생성 지연)와 동시 요청 시 5초로는 부족해 타임아웃→폴백이 잦았다.
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        // TODO: 로그인/회원가입 및 JWT/세션이 생기면 Authorization 헤더 인터셉터를 추가한다.
        .build()

    private val retrofit = Retrofit.Builder()
        // 서버 주소는 ServerConfig 한 파일에서 관리한다.
        .baseUrl(ServerConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val hyConnectService: HyConnectService = retrofit.create(HyConnectService::class.java)
}
