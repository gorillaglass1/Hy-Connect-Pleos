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

## 5) 현재 MainActivity 코드 설명

파일: `app/src/main/java/com/x/myapplication/MainActivity.kt`

### 동작 요약
- `MainActivity`는 `Activity`를 상속합니다.
- `onCreate`에서 화면 전체 UI를 코드로 구성합니다.
- 세로(`VERTICAL`) 정렬의 `LinearLayout`을 만들고 중앙 정렬(`Gravity.CENTER`) 및 패딩을 적용합니다.
- 다음 뷰를 순서대로 생성/추가합니다.
  1. 제목 `TextView`: `"하이커넥트"`, 글자 크기 `32f`
  2. 부제목 `TextView`: `"가까운 수소충전소를 추천합니다"`, 글자 크기 `18f`
  3. `Button`: `"충전소 찾기"`
- 마지막에 `setContentView(layout)`으로 화면에 표시합니다.

### 현재 상태
- 버튼 클릭 이벤트는 아직 연결되지 않았습니다.
- 즉, 현재 화면은 "정적 UI 표시" 단계입니다.

## 6) 팀 작업 시 참고사항
- UI를 빠르게 실험할 때는 현재처럼 코드 기반 레이아웃이 편리합니다.
- 기능 확장 시에는 아래 중 한 가지로 정리 권장:
  - XML 레이아웃 + ViewBinding
  - Jetpack Compose
- 패키지/네임스페이스 정보가 파일 간 다를 수 있으니(예: `MainActivity` 패키지와 Gradle namespace) 리팩터링 시 일관성 확인이 필요합니다.
