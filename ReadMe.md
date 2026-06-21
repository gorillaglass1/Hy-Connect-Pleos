# MyApplication 팀 개발 가이드

## 1) 프로젝트 개요
- Android 기본 앱 프로젝트입니다.
- 현재 메인 화면은 XML 레이아웃 대신 `MainActivity`에서 코드로 UI를 직접 생성합니다.

## 2) 개발 환경 초기 설정

### 필수 설치
- Android Studio (최신 안정 버전 권장)
- Android SDK Platform 36
- Android SDK Build-Tools
- Android Emulator
- JDK 11 (프로젝트 `sourceCompatibility`/`targetCompatibility` 기준)

### 프로젝트 열기
1. Android Studio 실행
2. `MyApplication` 폴더 열기
3. Gradle Sync 완료 대기

### Gradle/SDK 참고 값
- `compileSdk = 36`
- `targetSdk = 36`
- `minSdk = 28`
- AGP: `9.2.0`

## 3) 에뮬레이터 설정

### AVD 생성
1. Android Studio 우측 상단 `Device Manager` 진입
2. `Create Device` 선택
3. 기기 예시: `Pixel 7` (팀 표준으로 하나 정해서 통일 권장)
4. System Image 선택:
   - API Level 36 이미지 권장
   - 없으면 API 34 이상으로 임시 진행 가능
5. AVD 이름 지정 후 `Finish`

### 에뮬레이터 실행
1. Device Manager에서 생성한 AVD의 `Start` 클릭
2. 부팅 완료 후, 실행 타겟으로 해당 에뮬레이터 선택

## 4) 앱 실행 방법

### Android Studio에서 실행
1. 실행 타겟(에뮬레이터) 선택
2. `Run app` 클릭

### 터미널에서 실행
```bash
./gradlew assembleDebug
./gradlew installDebug
```

참고: `installDebug`는 에뮬레이터가 실행 중이고 기기가 정상 연결된 상태여야 합니다.

### 서버 주소 설정 (ServerConfig.kt)
- 통신 대상 서버 주소는 `app/src/main/java/com/hyconnect/pleos/data/network/ServerConfig.kt` **한 파일**에서 관리합니다.
- 이 파일은 로컬 환경마다 값이 달라 `.gitignore`로 제외되어 있으므로 **저장소에 포함되지 않습니다.** 새로 클론한 환경에서는 직접 만들어야 빌드됩니다.
- 아래 내용으로 파일을 생성하고 `BASE_URL`만 환경에 맞게 수정하세요 (Retrofit 규칙상 반드시 `/`로 끝나야 함):
  ```kotlin
  package com.hyconnect.pleos.data.network

  object ServerConfig {
      // 에뮬레이터에서 호스트 PC의 localhost는 10.0.2.2로 접근한다.
      // 실기기에서 같은 PC 서버에 붙으려면 PC의 LAN IP(예: http://192.168.0.10:8000/)로 바꾼다.
      const val BASE_URL: String = "http://10.0.2.2:8000/"
  }
  ```

### 더미 데이터 전환
- `debug` 빌드는 `BuildConfig.USE_DUMMY_DATA = true`로 동작하며, 서버나 Pleos SDK 승인 없이 화면을 확인할 수 있습니다.
- `release` 빌드는 `BuildConfig.USE_DUMMY_DATA = false`로 동작하며, `ApiClient`의 실제 API를 사용합니다.
- 더미 데이터를 완전히 제거할 때는 `app/build.gradle.kts`의 `USE_DUMMY_DATA` 필드와 `DummyHyConnectRepository`, `DummyHyConnectData`를 삭제하면 됩니다.

### 내비게이션 연동
- 현재 `경유지 추가` 버튼은 충전소 좌표가 있을 때 Pleos NaviHelper SDK의 `addWaypoint(RequestWaypointInfo)`로 Navigation 앱에 경유지를 추가합니다.
- Pleos 문서 기준 별도 인증 흐름 없이 `ai.pleos.playground:NaviHelper:2.0.3` 의존성과 `pleos.car.permission.NAVI_ROUTE`, `pleos.car.permission.NAVI_ROUTE_SEARCH` 등 매니페스트 권한으로 연동합니다.
- NaviHelper 호출이 실패하는 환경에서는 Android `geo:` 인텐트 폴백으로 충전소 위치를 열어 화면 검증을 이어갈 수 있습니다.

## 5) 현재 MainActivity 코드 설명

파일: `app/src/main/java/com/hyconnect/pleos/MainActivity.kt`

### 동작 요약
- `MainActivity`는 `ComponentActivity`를 상속합니다.
- `HyConnectViewModel`의 `uiState`를 수집해 Compose 기반 `HyConnectScreen`에 전달합니다.
- `경유지 추가` 버튼은 `NavigationClient`를 통해 Pleos Navigation에 충전소를 경유지로 추가합니다.
- 음성 호출, 설정, 더보기는 아직 프로토타입 Toast로 동작합니다.

### 현재 상태
- `debug` 빌드에서는 더미 차량 상태, AI 추천, 충전소 목록으로 앱을 바로 실행할 수 있습니다.
- 실제 서버 연동은 `release` 빌드의 `HyConnectRepositoryImpl` 경로를 사용합니다.

## 6) 팀 작업 시 참고사항
- UI는 Jetpack Compose 기준으로 확장합니다.
- 향후 경로 요청 방식이 바뀌어도 `NavigationClient`, `HyConnectRepository` 인터페이스 경계는 유지하고 구현체만 교체하는 방식이 가장 단순합니다.
