package com.hyconnect.pleos

import android.app.Application
import com.hyconnect.pleos.BuildConfig
import com.hyconnect.pleos.data.network.ApiClient
import com.hyconnect.pleos.data.repository.DummyHyConnectData
import com.hyconnect.pleos.data.repository.DummyHyConnectRepository
import com.hyconnect.pleos.data.repository.HyConnectRepository
import com.hyconnect.pleos.data.repository.HyConnectRepositoryImpl
import com.hyconnect.pleos.vehicle.habit.DrivingHabitStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HyConnectApplication : Application() {
    val repository: HyConnectRepository by lazy {
        if (BuildConfig.USE_DUMMY_DATA) {
            DummyHyConnectRepository()
        } else {
            HyConnectRepositoryImpl(ApiClient.hyConnectService)
        }
    }

    // 운전습관 로컬 영속 저장소(DataStore). ViewModel과 SDK 신호 라우팅이 공유한다.
    val drivingHabitStore: DrivingHabitStore by lazy { DrivingHabitStore(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // 단독 실행(더미 모드)에서는 SDK 주행 신호가 없으므로 더미 운전습관으로 채워 화면을 검증한다.
        if (BuildConfig.USE_DUMMY_DATA) {
            appScope.launch { drivingHabitStore.seedIfEmpty(DummyHyConnectData.drivingHabit) }
        }
    }
}
