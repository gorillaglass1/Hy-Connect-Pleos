package com.hyconnect.pleos

import android.app.Application
import com.hyconnect.pleos.data.network.ApiClient
import com.hyconnect.pleos.data.repository.HyConnectRepository
import com.hyconnect.pleos.data.repository.HyConnectRepositoryImpl

class HyConnectApplication : Application() {
    val repository: HyConnectRepository by lazy {
        HyConnectRepositoryImpl(ApiClient.hyConnectService)
    }
}
