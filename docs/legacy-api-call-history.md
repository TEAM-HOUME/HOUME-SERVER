# Legacy API Call History

## Purpose

삭제 후보 API의 실제 호출 여부를 DB에서 기간 단위로 확인한다.
삭제 후보의 실행 기준은 컨트롤러 메서드의 `@LegacyApi`다. Swagger의 `[DEPRECATED_CANDIDATE]` 표기는 API 소비자를 위한 안내이며 호출 이력 적재 조건으로 사용하지 않는다.

## Storage

테이블: `legacy_api_call_histories`

| Field | Stored Value | Policy |
| --- | --- | --- |
| `method` | HTTP method | 원문 보관 |
| `request_uri` | 실제 요청 URI path | query string은 제외 |
| `user_id` | 인증된 사용자 ID | 인증되지 않은 요청은 `NULL` |
| `trace_id` | 요청 추적 식별자 | 오류 응답 및 `X-Trace-Id` 헤더와 연결 가능 |
| `created_at` | 이력 생성 시각 | `BaseEntity` 감사 필드 사용 |

query, 요청 본문, 헤더, IP 주소는 저장하지 않는다. API별 호출 여부와 기간 집계, 호출 사용자 확인을 위한 최소 이력이다.

## Operating Flow

1. 매월 또는 삭제 이슈 검토 시 최근 30일과 90일을 `method`, `request_uri` 기준으로 집계한다.
2. 호출이 있으면 해당 API의 외부 소비자와 전환 필요 여부를 별도 확인한다.
3. 90일 동안 호출이 없는 API도 배치·외부 소비자 여부를 별도로 확인한 후 삭제 이슈에서 최종 판단한다.
4. 이력 테이블의 보존 기간과 자동 정리 작업은 호출량을 관찰한 뒤 별도 이슈에서 결정한다.

```sql
select method,
       request_uri,
       count(*) as call_count
from legacy_api_call_histories
where created_at >= now() - interval '30 days'
group by method, request_uri
order by call_count desc, request_uri;
```
