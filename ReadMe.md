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

> ⚠️ **클론 후 반드시 `ServerConfig.kt`를 직접 만들어야 빌드됩니다.**
> 이 파일은 환경마다 값이 달라 `.gitignore`로 제외되어 저장소에 포함되지 않습니다. `ApiClient`가 `ServerConfig.BASE_URL`을 참조하므로, 파일이 없으면 컴파일 단계에서 실패합니다.
>
> - **생성 위치:** `app/src/main/java/com/hyconnect/pleos/data/network/ServerConfig.kt`
> - **내용:** 아래 코드로 파일을 만들고 `BASE_URL`만 환경에 맞게 수정합니다. Retrofit 규칙상 주소는 반드시 `/`로 끝나야 합니다.
>
> ```kotlin
> package com.hyconnect.pleos.data.network
>
> object ServerConfig {
>     // 에뮬레이터에서 호스트 PC의 localhost는 10.0.2.2로 접근한다.
>     // 실기기에서 같은 PC 서버에 붙으려면 PC의 LAN IP(예: http://192.168.0.10:8000/)로 바꾼다.
>     const val BASE_URL: String = "http://10.0.2.2:8000/"
> }
> ```
>
> - `BASE_URL` 값은 통신 대상 서버 주소이며, **에뮬레이터**는 호스트 PC의 localhost를 `10.0.2.2`로 접근합니다. **실기기**에서 같은 PC 서버에 붙으려면 PC의 LAN IP(예: `http://192.168.0.10:8000/`)로 바꿉니다.
>
> 더 자세한 설명은 아래 [서버 주소 설정 (ServerConfig.kt)](#서버-주소-설정-serverconfigkt) 항목을 참고하세요.

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

### 음성 안내·음성 입력 (Gleo AI TTS/STT)
- 충전소 추천 시/선택 시 음성 안내를 출력하고(`VoiceGuideClient`), 마이크 버튼으로 자연어 음성 입력을 받습니다(`VoiceInputClient`).
- 정책상 **OnDevice 모드만** 사용합니다(`ai.pleos.playground:TextToSpeech`, `ai.pleos.playground:SpeechToText`). OnDevice는 `registerApp`(서버 인증)이 필요 없습니다.
- 음성은 **Pleos Connect 에뮬레이터**(또는 실차)에서만 실제로 동작합니다. 일반 AVD에서는 SDK 호출이 조용히 무시(no-op)되고 앱은 정상 동작합니다.

#### Pleos Connect 에뮬레이터에서 음성 사용하기
1. **시스템 이미지/AVD**: `Pleos Connect v2.0`(API 34) 이미지로 Automotive AVD를 만들고, 고급 설정에서 **Boot option = Cold boot**, Graphics = Hardware로 둡니다(차량 SDK와 동일).
2. **마이크 입력 허용(STT용)**: 에뮬레이터 우측 `...`(Extended Controls) → **Microphone** → "Virtual microphone uses host audio input"을 켭니다. macOS는 시스템 설정에서 에뮬레이터(qemu)의 마이크 권한도 허용해야 합니다.
3. **CRN 주입**: Pleos Playground > My Project > Project Info > **Test Info**에서 CRN을 복사한 뒤 주입합니다. CRN이 있어야 음성 서비스·차량 VHAL이 동작합니다. CRN은 비밀값이라 커밋하지 않습니다.
   ```bash
   # 방법 A: 인자로 전달
   scripts/inject-crn.sh --crn <복사한_CRN>
   # 방법 B: local.properties에 PLEOS_CRN=<CRN> 한 줄 추가 후
   scripts/inject-crn.sh
   ```
   스크립트는 내부적으로 다음을 수행합니다(에뮬레이터가 실행 중이어야 함):
   ```bash
   adb root
   adb shell su 0 "echo 'propId: 554696961 areaId: 0 values: <CRN>' > /data/vendor/vsomeip/vhal_fifo"
   adb reboot
   ```
4. 재부팅이 끝나면 `./gradlew installDebug`로 앱을 올리고 음성을 확인합니다.

#### 음성 기능 테스트 가이드
사전 준비: 위 1~4단계(Pleos Connect 에뮬 + CRN 주입 + 앱 설치) 완료, 호스트 스피커/마이크 사용 가능.

**TTS(음성 안내) 테스트**
1. 앱을 실행한다. 주행가능거리가 임계값(100km) 이하이면 자동으로 충전소 추천(LOW) 화면이 뜬다.
   - 에뮬에서 거리를 낮추려면 Vehicle VHAL의 `RANGE_REMAINING`을 작게 주입하거나, 추천 화면에서 검색을 실행한다.
2. 추천 목록이 채워지는 순간 "가장 가까운 곳은 OO충전소, N킬로미터 거리예요" 안내가 **스피커로 재생**된다.
3. 목록에서 충전소를 탭하면 "OO충전소를 경유지로 추가할까요?" 안내가 재생되고 확인 팝업이 뜬다.
4. 로그로 확인:
   ```bash
   adb logcat -s HyConnect.Tts            # 앱 측: initialize / speak 흐름
   adb logcat | grep -i "OnDeviceTtsStreamManagerImpl\|writeAudioTrack"  # CaaS 측: 실제 합성/재생
   ```
   `writeAudioTrack ... start seoyoon ko_KR` + `AudioTrack ... frames delivered` 가 보이면 정상 재생된 것이다.

**STT(마이크 음성 입력) 테스트**
1. 추천(LOW) 화면의 검색창 오른쪽 **🎙 마이크 버튼**을 누른다(에뮬 멀티윈도우에서는 `adb input tap`이 불안정하니 직접 클릭 권장).
2. "듣고 있어요. 말씀해 주세요." 토스트가 뜨면 호스트 마이크에 대고 "제일 가까운 충전소" 같이 말한다.
   - 에뮬 마이크는 Extended Controls → Microphone에서 "host audio input"이 켜져 있어야 한다.
3. 인식이 끝나면 그 문장으로 충전소 재검색이 실행되고, 결과에 대해 다시 TTS 안내가 나온다.
4. 로그로 확인:
   ```bash
   adb logcat -s HyConnect.Stt            # initialize / request / 최종 인식 문장
   adb logcat | grep -i "OnDeviceStt\|recognition"   # CaaS STT 인식 동작
   ```

**자주 겪는 문제**
- 소리가 안 남: CRN 미주입(가장 흔함) → `scripts/inject-crn.sh` 재실행 후 재부팅. 호스트 스피커 음소거 확인.
- `HyConnect.Tts: TTS initialize 실패` 로그: SDK 서비스 미존재(일반 AVD) → Pleos Connect 이미지로 실행.
- STT가 인식 안 됨: 에뮬 마이크(host audio input) 비활성 또는 macOS 마이크 권한 차단.

## 5) 현재 MainActivity 코드 설명

파일: `app/src/main/java/com/hyconnect/pleos/MainActivity.kt`

### 동작 요약
- `MainActivity`는 `ComponentActivity`를 상속합니다.
- `HyConnectViewModel`의 `uiState`를 수집해 Compose 기반 `HyConnectScreen`에 전달합니다.
- `경유지 추가` 버튼은 `NavigationClient`를 통해 Pleos Navigation에 충전소를 경유지로 추가합니다.
- 마이크 버튼은 Gleo AI STT(OnDevice)로 음성 입력을 받아 충전소 검색에 사용합니다. 설정·더보기는 아직 프로토타입 Toast로 동작합니다.

### 현재 상태
- `debug` 빌드에서는 더미 차량 상태, AI 추천, 충전소 목록으로 앱을 바로 실행할 수 있습니다.
- 실제 서버 연동은 `release` 빌드의 `HyConnectRepositoryImpl` 경로를 사용합니다.

## 6) 팀 작업 시 참고사항
- UI는 Jetpack Compose 기준으로 확장합니다.
- 향후 경로 요청 방식이 바뀌어도 `NavigationClient`, `HyConnectRepository` 인터페이스 경계는 유지하고 구현체만 교체하는 방식이 가장 단순합니다.
