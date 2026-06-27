package com.hyconnect.pleos.vehicle.habit

/**
 * 운전습관 분석에 필요한 차량 신호를 받는 싱크.
 * [VehicleSdkClient]가 속도(android.car CarProperty)·조향각(Pleos SDK)·주행상태(기어)를 모아 전달하고,
 * 구현체(보통 ViewModel)가 [DrivingHabitAnalyzer]로 라우팅한다.
 */
interface DrivingHabitSignalListener {
    /** 속도 통지. 단위는 m/s. */
    fun onSpeed(metersPerSec: Float)

    /** 조향각 통지. 단위는 도(deg). */
    fun onSteeringAngle(angleDeg: Float)

    /** 주행/정차 상태 변경. true면 주행 중(세션 시작), false면 정차/주차(세션 종료). */
    fun onDrivingStateChanged(isDriving: Boolean)
}
