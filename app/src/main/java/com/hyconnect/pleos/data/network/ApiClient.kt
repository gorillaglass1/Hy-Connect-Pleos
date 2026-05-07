package com.hyconnect.pleos.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // TODO: 실제 배포 서버 주소가 확정되면 BuildConfig 또는 원격 설정으로 교체한다.
    private const val BASE_URL = "https://api.myhyconnect.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        // TODO: 인증/회원가입 로직이 붙으면 Authorization 헤더 인터셉터를 추가한다.
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val hyConnectService: HyConnectService = retrofit.create(HyConnectService::class.java)
}
