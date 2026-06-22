package com.hyconnect.pleos.vehicle

/**
 * 차량의 주행/정차 상태. VehicleSpeedListener(또는 기어 상태)에서 도출한다.
 *
 * UNKNOWN: 아직 SDK가 첫 통지를 보내지 않았거나 판별 불가.
 *          정책상 안전 측 동작(액션 없음)을 한다.
 */
enum class VehicleDrivingState {
    DRIVING,
    STOPPED,
    UNKNOWN,
}
