package com.hyconnect.pleos.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.refreshSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "refresh_settings",
)

/**
 * 화면별 자동 새로고침(주기적 재요청) 주기 설정.
 *
 * 연료가 적은 추천 화면(LOW)에서는 주행으로 위치가 바뀌어도 현재 위치 기준 충전소를 다시 추천하도록
 * 일정 주기마다 추천을 재요청한다. 메인 대시보드(SUFFICIENT)도 같은 방식으로 주기 갱신할 수 있다.
 *
 * 값의 의미(초):
 * - 0(= [DISABLED]): 비활성화. 자동 새로고침을 하지 않는다.
 * - 그 외 양수: 해당 초마다 재요청한다.
 *
 * 기본값: 추천 화면 300초, 메인 대시보드 비활성화.
 */
data class RefreshSettings(
    val lowRefreshSec: Int = DEFAULT_LOW_REFRESH_SEC,
    val dashboardRefreshSec: Int = DEFAULT_DASHBOARD_REFRESH_SEC,
) {
    companion object {
        /** 비활성화를 의미하는 주기 값. */
        const val DISABLED = 0

        /** 추천 화면(LOW) 기본 주기: 300초. */
        const val DEFAULT_LOW_REFRESH_SEC = 300

        /** 메인 대시보드(SUFFICIENT) 기본값: 비활성화. */
        const val DEFAULT_DASHBOARD_REFRESH_SEC = DISABLED
    }
}

/** 자동 새로고침 주기 설정을 로컬(DataStore Preferences)에 영속 저장한다. */
class RefreshSettingsStore(private val context: Context) {

    val settings: Flow<RefreshSettings> = context.refreshSettingsDataStore.data
        .catch { e ->
            // DataStore I/O 오류 시 기본값으로 폴백(앱이 죽지 않게).
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            RefreshSettings(
                lowRefreshSec = prefs[Keys.LOW] ?: RefreshSettings.DEFAULT_LOW_REFRESH_SEC,
                dashboardRefreshSec = prefs[Keys.DASHBOARD] ?: RefreshSettings.DEFAULT_DASHBOARD_REFRESH_SEC,
            )
        }

    /** 추천 화면(LOW) 자동 새로고침 주기(초)를 저장한다. 0이면 비활성화. */
    suspend fun setLowRefreshSec(seconds: Int) {
        context.refreshSettingsDataStore.edit { it[Keys.LOW] = seconds.coerceAtLeast(0) }
    }

    /** 메인 대시보드(SUFFICIENT) 자동 새로고침 주기(초)를 저장한다. 0이면 비활성화. */
    suspend fun setDashboardRefreshSec(seconds: Int) {
        context.refreshSettingsDataStore.edit { it[Keys.DASHBOARD] = seconds.coerceAtLeast(0) }
    }

    private object Keys {
        val LOW = intPreferencesKey("low_refresh_sec")
        val DASHBOARD = intPreferencesKey("dashboard_refresh_sec")
    }
}
