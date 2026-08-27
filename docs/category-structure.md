# 상품 카테고리 구조 및 매핑 분석

## 1. 카테고리 체계 개요

HOUME-SERVER의 상품 카테고리는 **두 개의 독립된 분류 체계**가 공존한다.

```
외부 체계 (SoozipCategory)          내부 체계 (FurnitureType → Furniture)
─────────────────────────          ──────────────────────────────────────
출처 쇼핑몰 기준 대분류 (6개)         AI 이미지 생성/큐레이션 필터 기준 분류
CurationRawProduct.category        CurationRawProduct ↔ 조인 테이블 ↔ Furniture
직접 저장 (STRING enum)             간접 연결 (다대다 관계)
```

두 체계 사이에 **직접 매핑 로직은 없다.**

---

## 2. SoozipCategory (외부 카테고리)

**위치:** `houme-domain/.../furniture/model/entity/SoozipCategory.java`

| Enum 값 | cateNo | 한글 레이블 |
|---|---|---|
| `MINI_ELECTRONICS` | 73 | 미니가전 |
| `FURNITURE` | 75 | 가구 |
| `LIGHTING` | 76 | 조명 |
| `LIVING_GOODS` | 86 | 생활용품 |
| `HOME_FABRIC` | 47 | 홈패브릭 |
| `ACCESSORY` | 52 | 소품 |

`cateNo`는 Soozip API의 카테고리 번호. DB에는 enum 이름(예: `"FURNITURE"`)이 STRING으로 저장.

**입력 경로:**
- **어드민 API** — `POST /api/v1/admin/curation-raw-products` 등록 시 직접 지정
- **네이버 쇼핑 크롤러** — `CurationRawProductServiceImpl.saveAll()` 호출 시 enum 명시적 전달 (네이버 API 자체에는 카테고리 정보 없음, 크롤 시점에 결정)

**제약:** `nullable = false`, `(source, category, product_id)` unique constraint.

---

## 3. 내부 분류 체계 (FurnitureType → Furniture)

**2단계 계층 구조:**

```
FurnitureType (1단계)
  └─ Furniture (2단계)
```

### FurnitureType (실제 DB 데이터)

| id | furniture_type | name_kr | name_eng | is_required |
|---|---|---|---|---|
| 1 | BED | 침대 | BED | true |
| 2 | (없음) | 소파 | SOFA | false |
| 4 | (없음) | 테이블 | TABLE | false |
| 5 | SELECTIVE | 그 외 | SELECTIVE | false |
| 7 | (없음) | 기타 | ETC | - |

### Furniture (실제 DB 데이터)

| id | furniture_name_eng | furniture_name_kr | furniture_type_id (→ 타입) |
|---|---|---|---|
| 1 | SINGLE | 싱글 침대 | 1 (BED) |
| 2 | SUPER_SINGLE | 슈퍼싱글 침대 | 1 (BED) |
| 3 | DOUBLE | 더블 침대 | 1 (BED) |
| 4 | QUEEN_OVER | 퀸 침대 이상 | 1 (BED) |
| 8 | SINGLE_SOFA | 1인용 소파 | 2 (SOFA) |
| 19 | TWO_SEATER_SOFA | 2인용 소파 | 2 (SOFA) |
| 22 | CHAIR | 의자/스툴 | 2 (SOFA) |
| 5 | OFFICE_DESK | 업무용 책상 | 4 (TABLE) |
| 7 | DINING_TABLE | 식탁 | 4 (TABLE) |
| 15 | SITTING_TABLE | 좌식 테이블 | 4 (TABLE) |
| 25 | DRESSING_TABLE | 화장대/협탁 | 4 (TABLE) |
| 10 | MOVABLE_TV | 이동식 TV | 5 (SELECTIVE) |
| 16 | MIRROR | 전신 거울 | 5 (SELECTIVE) |
| 17 | WHITE_BOOKSHELF | 책 선반 | 5 (SELECTIVE) |
| 18 | DISPLAY_CABINET | 장식장 | 5 (SELECTIVE) |
| 24 | LIGHTING | 조명 | 5 (SELECTIVE) |
| 27 | CLOSET | 옷장 | 5 (SELECTIVE) |
| 28 | ETC | 기타 | 7 (ETC) |

**위치:** `houme-infra/.../furniture/model/entity/`

---

## 4. CurationRawProduct와 내부 분류 연결 구조

```
CurationRawProduct
  │
  ├─── CurationRawProductFurnitureTag (조인 테이블)
  │         └─── FurnitureTag
  │                   └─── Furniture → FurnitureType
  │
  └─── CurationRawProductFurniture (직접 조인 테이블)
            └─── Furniture → FurnitureType
```

- 하나의 상품은 **여러 Furniture에 태그**될 수 있음 (다대다)
- `FurnitureTag`는 가구별 스타일 프롬프트 단위
- 두 경로(Tag 경유 / 직접 매핑) 모두 최종적으로 `Furniture → FurnitureType`으로 귀결

---

## 5. 큐레이션 필터에서의 카테고리 동작

**필터 흐름:**
```
클라이언트 typeIds (FurnitureType.id 또는 Furniture.id + 10000)
  │
  ▼
typeFilterIds (< 10000) → FurnitureType 기준 필터
furnitureFilterIds (≥ 10000, offset 제거) → Furniture 기준 필터
  │
  ▼
QueryDSL 조인:
CurationRawProduct
  → CurationRawProductFurnitureTag
    → FurnitureTag
      → Furniture
        → FurnitureType
  │
  ▼
매칭되는 상품 ID 집합으로 필터링
```

- `SoozipCategory`는 이 필터링에 **관여하지 않음**
- offset `10000`을 기준으로 `FurnitureType.id`와 `Furniture.id`를 하나의 배열로 통합 처리
- `ETC(기타)` 타입은 별도 `resolveEtc()` 로직으로 처리

**V2 최적화:** `searchTokens` 컬럼(상품명+브랜드+가구유형명 토큰화 저장)을 활용해 조인 없이 텍스트 검색. pg_trgm GIN 인덱스 사용.

---

## 6. 두 체계 비교 요약

| 항목 | SoozipCategory | FurnitureType / Furniture |
|---|---|---|
| 기준 | 외부 쇼핑몰(Soozip) 분류 | 하우미 내부 AI/큐레이션 분류 |
| 개수 | 6개 enum | FurnitureType 5개 + Furniture 18개 |
| 저장 방식 | CurationRawProduct 직접 컬럼 | 조인 테이블(다대다) |
| 사용처 | 카테고리별 추천, 크롤러 중복 방지 unique key | 큐레이션 필터 UI, AI 이미지 생성 |
| 직접 매핑 | **없음** | — |

---

## 7. 두 체계 간 대응 관계 (실제 데이터 기준)

| SoozipCategory | 대응 FurnitureType | 비고 |
|---|---|---|
| FURNITURE (가구) | BED + SOFA + TABLE + SELECTIVE 전부 | **너무 광범위, 1:1 매핑 불가** |
| LIGHTING (조명) | SELECTIVE 하위 `LIGHTING` (id=24) 하나 | 타입이 아닌 개별 Furniture 항목 |
| HOME_FABRIC (홈패브릭) | 없음 | 내부 분류에 대응 없음 |
| ACCESSORY (소품) | 없음 | 내부 분류에 대응 없음 |
| LIVING_GOODS (생활용품) | 없음 | 내부 분류에 대응 없음 |
| MINI_ELECTRONICS (미니가전) | 없음 | 내부 분류에 대응 없음 |

**결론:** 두 체계는 1:1로 매핑되지 않는다. `SoozipCategory.FURNITURE`가 내부 FurnitureType 대부분을 포괄하고, LIGHTING만 부분적으로 겹친다. 나머지 4개(HOME_FABRIC, ACCESSORY, LIVING_GOODS, MINI_ELECTRONICS)는 내부 분류에 대응이 없다.

---

## 8. 가격비교 파이프라인(C-1)에서의 함의

**확정 사항 (v2 핸드오프, 2026-08-24 기준):**

하드 필터 기준으로 **SoozipCategory 확정.** eBay·쿠팡·자체카탈로그 3개 소스 모두 동일 기준 적용.

```
원본 상품의 SoozipCategory
  →  소스별 카테고리 ID로 변환 (아래 매핑표)
  →  검색 요청 또는 응답 후처리 시 하드 필터 적용
  →  매칭되는 상품만 최종 결과에 포함
```

### eBay category_id 매핑 (예비안, Taxonomy API 미검증)

| SoozipCategory | eBay category_id | 비고 |
|---|---|---|
| FURNITURE (가구) | 3197 | Furniture |
| LIGHTING (조명) | 20697 | Lamps, Lighting & Ceiling Fans |
| HOME_FABRIC (홈패브릭) | 20444 | Bedding |
| ACCESSORY (소품) | 미확정 | |
| MINI_ELECTRONICS (미니가전) | 미확정 | |
| LIVING_GOODS (생활용품) | 미확정 | |

**주의:** eBay Browse API는 요청당 category_id 1개만 허용 (복수 지정 시 `allowedMaxCategories: 1` 에러). 따라서 하드 필터는 **응답 후처리**로 구현 (요청 파라미터가 아닌 결과 필터링).

쿠팡 캐시 배치 단계에서는 `soozip_ebay_category_mapping` 테이블에 매핑 저장 예정.

---

*작성일: 2026-08-19 | 최종 업데이트: 2026-08-24 (v2 핸드오프 반영) | 분석 기준 브랜치: develop*
