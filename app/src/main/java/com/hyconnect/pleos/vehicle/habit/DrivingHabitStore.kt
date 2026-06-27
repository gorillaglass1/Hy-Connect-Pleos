package com.hyconnect.pleos.vehicle.habit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.drivingHabitDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "driving_habit",
)

/**
 * 운전습관 통산 프로파일을 로컬(DataStore Preferences)에 영속 저장한다.
 *
 * - 세션 종료 시 [recordSession]으로 누적한다.
 * - [reset]은 "주행습관 기록 초기화" 버튼이 호출한다(전체 카운트 삭제).
 * - [profile]은 화면이 실시간으로 구독하고, [snapshot]은 서버 요청 직전 1회성 조회에 쓴다.
 */
class DrivingHabitStore(private val context: Context) {

    val profile: Flow<DrivingHabitProfile> = context.drivingHabitDataStore.data
        .catch { e ->
            // DataStore I/O 오류 시 빈 값으로 폴백(앱이 죽지 않게).
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { it.toProfile() }

    suspend fun snapshot(): DrivingHabitProfile = profile.first()

    suspend fun recordSession(session: DrivingSession, nowMs: Long = System.currentTimeMillis()) {
        context.drivingHabitDataStore.edit { prefs ->
            val updated = prefs.toProfile().accumulate(session, nowMs)
            prefs[Keys.SESSIONS] = updated.totalSessions
            prefs[Keys.MINUTES] = updated.totalDrivingMinutes
            prefs[Keys.ACCEL] = updated.harshAccelCount
            prefs[Keys.BRAKE] = updated.harshBrakeCount
            prefs[Keys.INCAUTIOUS] = updated.incautiousCount
            prefs[Keys.CUM_SCORE] = updated.cumulativeScore
            prefs[Keys.UPDATED] = updated.lastUpdatedEpochMs
        }
    }

    /** 모든 운전습관 기록을 삭제한다. */
    suspend fun reset() {
        context.drivingHabitDataStore.edit { it.clear() }
    }

    /**
     * 기록이 비어 있을 때만 더미 프로파일로 채운다(단독 실행/데모용).
     * 이미 실제 기록이 있으면 아무것도 하지 않는다.
     */
    suspend fun seedIfEmpty(profile: DrivingHabitProfile, nowMs: Long = System.currentTimeMillis()) {
        context.drivingHabitDataStore.edit { prefs ->
            if ((prefs[Keys.SESSIONS] ?: 0) > 0) return@edit
            prefs[Keys.SESSIONS] = profile.totalSessions
            prefs[Keys.MINUTES] = profile.totalDrivingMinutes
            prefs[Keys.ACCEL] = profile.harshAccelCount
            prefs[Keys.BRAKE] = profile.harshBrakeCount
            prefs[Keys.INCAUTIOUS] = profile.incautiousCount
            prefs[Keys.CUM_SCORE] = profile.cumulativeScore
            prefs[Keys.UPDATED] = if (profile.lastUpdatedEpochMs == 0L) nowMs else profile.lastUpdatedEpochMs
        }
    }

    private fun Preferences.toProfile() = DrivingHabitProfile(
        totalSessions = this[Keys.SESSIONS] ?: 0,
        totalDrivingMinutes = this[Keys.MINUTES] ?: 0,
        harshAccelCount = this[Keys.ACCEL] ?: 0,
        harshBrakeCount = this[Keys.BRAKE] ?: 0,
        incautiousCount = this[Keys.INCAUTIOUS] ?: 0,
        cumulativeScore = this[Keys.CUM_SCORE] ?: 0L,
        lastUpdatedEpochMs = this[Keys.UPDATED] ?: 0L,
    )

    private object Keys {
        val SESSIONS = intPreferencesKey("total_sessions")
        val MINUTES = intPreferencesKey("total_driving_minutes")
        val ACCEL = intPreferencesKey("harsh_accel")
        val BRAKE = intPreferencesKey("harsh_brake")
        val INCAUTIOUS = intPreferencesKey("incautious")
        val CUM_SCORE = longPreferencesKey("cumulative_score")
        val UPDATED = longPreferencesKey("last_updated")
    }
}
