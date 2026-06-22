package com.hyconnect.pleos.location

/**
 * NaviHelper가 통지하는 현재 경로의 목적지를 앱 전역에서 공유하기 위한 저장소.
 *
 * - 쓰기: [com.hyconnect.pleos.navigation.PleosNaviHelperNavigationClient]의
 *   onDestinationInfo 콜백에서 [update]로 채우고, 경로 종료/취소/도착 시 [clear]로 비운다.
 * - 읽기: Repository가 충전소 추천 요청을 만들 때 [snapshot]으로 목적지 좌표를 가져간다.
 *   목적지가 없으면(=경로 안내 중이 아니면) null이며, 이때는 목적지 없이(비워서) 요청한다.
 *
 * NaviHelper 콜백(다른 스레드)과 Repository 읽기가 겹칠 수 있어 단순 동기화로 보호한다.
 */
data class Destination(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null,
)

object DestinationStore {
    @Volatile
    private var current: Destination? = null

    fun update(destination: Destination) {
        current = destination
    }

    /** 경로가 끝났거나 목적지가 없어진 경우 비운다. */
    fun clear() {
        current = null
    }

    /** 목적지가 없으면(경로 안내 중이 아니면) null. 호출 측에서 목적지를 비워서 요청한다. */
    fun snapshot(): Destination? = current
}
