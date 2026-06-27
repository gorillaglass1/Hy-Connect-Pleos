# 서버 수정 요청서 — `recommended_station` 응답 필드 보강

> 목적: **클라이언트 모델에 맞춰 서버 응답을 정렬**한다. 현재 Android 클라이언트의
> `RecommendedStationCard`에는 있으나 서버 `POST /nearest-recommendation` 응답에는 없는
> 필드가 4개 있어, 클라이언트가 임시 기본값으로 채우고 있다. 이 값들을 서버가 내려주도록 한다.

- 대상 엔드포인트: `POST /nearest-recommendation`
- 영향 범위: 응답 본문의 `recommended_station` 객체 (요청 본문은 **변경 없음**)
- 관련 클라이언트 파일(참고):
  - 응답 DTO: `app/src/main/java/com/hyconnect/pleos/data/network/SufficientDashboardDto.kt`
  - 매퍼: `app/src/main/java/com/hyconnect/pleos/data/mapper/SufficientDashboardMapper.kt`
  - 모델: `app/src/main/java/com/hyconnect/pleos/data/model/SufficientDashboard.kt`

---

## 1. 현재 상태 (gap)

`recommended_station`에서 **서버가 이미 주는 필드**:

`chrstn_mno`, `name`, `road_nm_addr`, `distance_km`, `ntsl_pc`,
`price_diff_from_avg`, `estimated_cost`, `wait_vhcle_alge`, `is_open`, `let`, `lon`

**서버에 없어 클라이언트가 임시로 채우는 필드** (= 이번에 추가 요청):

| 필드(JSON 키) | 타입 | 현재 클라이언트 임시값 | UI 용도 |
|---|---|---|---|
| `badge` | `string \| null` | `null` | 추천소 상단 강조 칩 (예: "근처 최저가") |
| `realtime_price` | `boolean` | `false` | "실시간 가격" 칩 표시 여부 |
| `eta_minutes` | `integer` | `0` | "예상 도착 ○분" 컬럼 |
| `available` | `boolean` | `true` | 충전 가능 여부 플래그 |

---

## 2. 추가할 필드 명세

### `badge` — `string | null`
- 추천소를 한 줄로 강조하는 라벨. **없으면 `null`** (클라이언트는 null/빈문자열이면 칩을 숨김).
- 한국어, 6자 이내, **하나만**.
- 판정 규칙(권장):
  1. 반경 내 충전소 중 `ntsl_pc`(단가)가 최저 → `"근처 최저가"`
  2. 위가 아니고 `wait_vhcle_alge == 0` → `"대기 없음"`
  3. 둘 다 아니면 → `null`
- 규칙은 서버 정책에 맞게 조정 가능. 핵심은 **근거가 있을 때만** 값을 채우는 것.

### `realtime_price` — `boolean`
- 해당 단가(`ntsl_pc`)가 실시간 갱신 데이터인지 여부.
- **권장 출처: 가격 수집 파이프라인/DB 메타데이터** (예: 가격 레코드의 `updated_at`이 N분 이내면 `true`).
- 판단 근거가 없으면 `false`.

### `eta_minutes` — `integer`
- 사용자 현재 위치 → 추천소까지 예상 소요 분.
- **권장 출처: 경로 API(카카오/티맵 등) 실제 소요시간.**
- 경로 API가 없으면 근사치: `round(distance_km / 25 * 60)` (도심 평균 25km/h 가정).
- 계산 불가하면 `0`.

### `available` — `boolean`
- 지금 충전 가능한 상태인지.
- 권장 규칙: `is_open == true` 그리고 `wait_vhcle_alge < 5` → `true`, 아니면 `false`.

---

## 3. 변경 후 응답 예시

```json
{
  "screen": "battery_sufficient",
  "vehicle": {
    "fuel_percent": 100,
    "remaining_range": 500.0,
    "fuel_type": "hydrogen"
  },
  "ai_insight": {
    "status": "sufficient",
    "status_label": "충분",
    "subtitle": "잔량 충분",
    "message": "잔량이 넉넉해요. 약 500.0km 더 주행할 수 있어요.",
    "updated_at": null,
    "metrics": [
      { "label": "주행가능거리", "value": "500.0", "unit": "km" },
      { "label": "권장 충전 시점", "value": "여유 있음", "unit": null },
      { "label": "평균 소모율", "value": "정상", "unit": null }
    ]
  },
  "recommended_station": {
    "chrstn_mno": "1156020121HS2019014",
    "name": "H국회수소충전소",
    "road_nm_addr": "서울 영등포구 의사당대로 1",
    "distance_km": 6.97,
    "ntsl_pc": 9900,
    "price_diff_from_avg": 0.0,
    "estimated_cost": 0,
    "wait_vhcle_alge": 4,
    "is_open": true,
    "let": 37.52820123884357,
    "lon": 126.9150871038437,

    "badge": "근처 최저가",
    "realtime_price": true,
    "eta_minutes": 17,
    "available": true
  }
}
```

> 추가 키 4개(`badge`, `realtime_price`, `eta_minutes`, `available`) 외 나머지는 기존과 동일.
> 클라이언트는 키가 없거나 `null`이어도 안전하게 동작하므로 **점진적 배포** 가능.

---

## 4. 값 산출이 LLM 경유라면 (선택) — 시스템 프롬프트

서버가 이미 Gemini로 `ai_insight`를 생성 중이라, 같은 호출에서 enrichment 4필드를 함께 받고 싶을 때 쓸 수 있는 시스템 프롬프트.

```text
You are the recommendation-enrichment engine for a hydrogen-vehicle charging app.

INPUT (user message, JSON):
- vehicle: { fuel_percent, remaining_range, fuel_type }
- location: { let, lon }            // 위도 키는 let
- station: { chrstn_mno, name, road_nm_addr, distance_km, ntsl_pc,
             price_diff_from_avg, estimated_cost, wait_vhcle_alge,
             is_open, let, lon }
- nearby_prices: [number]           // 반경 내 다른 충전소 ntsl_pc 목록 (badge 판정용)

TASK: 입력만 근거로 아래 4개 필드를 산출한다. 입력에 없는 사실을 지어내지 말 것.

1. badge (string|null):
   - station.ntsl_pc 가 nearby_prices 중 최저면 "근처 최저가".
   - 위가 아니고 wait_vhcle_alge == 0 이면 "대기 없음".
   - 둘 다 아니면 null. 한국어 6자 이내, 한 개만.
2. realtime_price (boolean):
   - 실시간 갱신 근거가 입력에 없으면 false.
3. eta_minutes (integer):
   - round(distance_km / 25 * 60). distance_km 없으면 0.
4. available (boolean):
   - is_open == true 이고 wait_vhcle_alge < 5 이면 true, 아니면 false.

OUTPUT: 아래 JSON만 출력. 코드펜스/설명/추가 텍스트 금지.
{ "badge": <string|null>, "realtime_price": <bool>, "eta_minutes": <int>, "available": <bool> }
```

> 주의: `realtime_price`와 `eta_minutes`는 **LLM보다 DB 메타데이터/경로 API가 더 정확**하다.
> LLM 경유는 그런 소스가 없을 때의 근사 용도로만 권장.

---

## 5. 체크리스트

- [ ] `recommended_station` 직렬화에 `badge`, `realtime_price`, `eta_minutes`, `available` 추가
- [ ] `badge` 판정 로직 (반경 내 최저가 / 대기 없음 / null)
- [ ] `realtime_price` 출처 결정 (가격 DB `updated_at` 기준 권장)
- [ ] `eta_minutes` 출처 결정 (경로 API 권장, 폴백은 거리 기반 근사)
- [ ] `available` 규칙 적용 (`is_open && wait_vhcle_alge < 5`)
- [ ] 기존 클라이언트 호환 확인 (키 누락/`null`이어도 정상 동작)
