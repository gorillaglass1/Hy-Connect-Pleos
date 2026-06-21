package com.hyconnect.pleos.location

/**
 * NaviHelper가 통지하는 현재 위치를 앱 전역에서 공유하기 위한 저장소.
 *
 * - 쓰기: [com.hyconnect.pleos.navigation.PleosNaviHelperNavigationClient]의
 *   onCurrentLocationInfo 콜백에서 [update]로 최신 좌표를 채운다.
 * - 읽기: Repository가 충전소 추천 요청을 만들 때 [snapshot]으로 현재 좌표를 가져간다.
 *
 * NaviHelper 위치 콜백(다른 스레드)과 Repository 읽기가 겹칠 수 있어 단순 동기화로 보호한다.
 */
data class CurrentLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
)

object CurrentLocationStore {
    @Volatile
    private var current: CurrentLocation? = null

    fun update(location: CurrentLocation) {
        current = location
    }

    /** 아직 위치를 한 번도 받지 못했으면 null. 호출 측에서 기본 좌표로 폴백한다. */
    fun snapshot(): CurrentLocation? = current
}
