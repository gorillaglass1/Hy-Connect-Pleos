package com.hyconnect.pleos.ui.theme

import androidx.compose.ui.graphics.Color

// PLEOS 샘플 앱(안전운전 점수 계산기)의 시스템 디자인 토큰을 그대로 차용한 팔레트.
// 기존 Hy* 이름은 유지해 모든 컴포넌트가 자동으로 샘플 룩을 따르도록 매핑한다.

// 시스템 서페이스
val HySurface = Color(0xFFFFFFFF)        // system_surface_basic
val HyBackground = Color(0xFFF7F8FA)     // system_surface_low
val HyTankRest = Color(0xFFF7F8FA)       // system_surface_low (보조 면)

// 라벨(그레이) 위계
val HyTextPrimary = Color(0xFF131417)    // system_label_gray_primary
val HyTextSecondary = Color(0xA31B1C1E)  // system_label_gray_teritiary
val HyTextMuted = Color(0x521B1C1E)      // system_label_gray_quaternary

// 디바이더
val HyBorder = Color(0x1A131417)         // system_divider_divider_secondary
val HyBorderStrong = Color(0x33131417)   // system_divider_divider_territary

// 인포메이티브 컬러
val HyBlue = Color(0xFF0064FF)           // system_informative_positive
val HyBlueDark = Color(0xFF0050CC)
val HyBlueSoft = Color(0xFFE5EFFF)       // system_informative_positive_blur
val HySuccess = Color(0xFF0064FF)

val HyPositive = Color(0xFF02C265)       // system_informative_active
val HyPositiveSoft = Color(0xFFE5F9F1)   // system_informative_active_blur

val HyWarn = Color(0xFFFE3D16)           // system_informative_negative
val HyWarnSoft = Color(0xFFFFECE8)       // system_informative_negative_blur
val HyWarnBorder = Color(0x33FE3D16)
