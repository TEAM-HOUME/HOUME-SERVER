# API Usage Audit

## Purpose

레거시 API와 실제 사용 API를 구분하기 위한 API 인벤토리 문서입니다.
`Status`는 프론트엔드 사용처, 서버 내부 SSR 어드민 화면, 운영 로그, 수동/도메인 확인 결과를 종합해 분류합니다.
삭제 후보는 즉시 삭제 확정이 아니라 후속 이슈에서 영향도와 전환 여부를 확인합니다.

## Status Guide

| Status | Meaning |
|---|---|
| ACTIVE | 현재 사용 중이거나 유지 필요가 확인됨 |
| LEGACY_USED | 구버전이지만 최근 호출 또는 의존성이 확인됨 |
| DEPRECATED_CANDIDATE | 미사용 또는 이전 버전 기능으로 확인된 삭제 후보 |
| INTERNAL_ONLY | 어드민, 운영 도구, 개발자 테스트 등 제한된 용도로만 사용 |
| UNKNOWN | 사용 여부 판단 근거 부족 |

## Audit Columns

| Column | Description |
|---|---|
| Status | 현재 사용 상태 |
| Method | HTTP method |
| Path | Backend endpoint path |
| Controller | 선언 위치 |
| Purpose | 코드 annotation, method name 기준의 현재 역할 |
| Frontend Path | 프론트엔드 코드에서 확인한 호출 path |
| Other Source | 운영 로그, Postman, 외부 문서, 어드민 화면 등 추가 근거 |
| Latest Alternative | 대체 가능한 최신 API |
| Action | 유지, 모니터링, deprecate, 삭제 이슈 생성 등 |

## Status Decision Rule

| Status | Current Rule |
|---|---|
| ACTIVE | 프론트엔드, 서버 내부 SSR, 운영 로그, 담당자 확인 중 하나로 유지 필요가 확인됨 |
| LEGACY_USED | 최신 대체 API가 있지만 프론트엔드 런타임 사용처 또는 운영 호출이 확인됨 |
| DEPRECATED_CANDIDATE | 미사용 또는 이전 버전 기능으로 확인되어 삭제 후보임 |
| INTERNAL_ONLY | 일반 앱 API가 아니라 운영/어드민 화면/헬스 확인/개발자 테스트 등 내부 용도 |
| UNKNOWN | 현재 근거만으로 유지 또는 삭제 후보 판단이 부족함 |

## Public And App APIs

| Status | Method | Path | Controller | Purpose | Frontend Path | Other Source | Latest Alternative | Action |
|---|---|---|---|---|---|---|---|---|
| INTERNAL_ONLY | GET | `/api/v1/env` | `HealthCheckController` | 서버 환경 확인 |  |  |  | 운영/헬스 확인 용도 여부 확인 |
| ACTIVE | GET | `/api/v1/landings` | `BannerController` | 랜딩 목록 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/banners/{bannerId}` | `BannerController` | 배너 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/banners/{bannerId}/detail` | `BannerController` | 배너 상세 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/other-styles` | `BannerController` | 다른 스타일 목록 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/other-styles/{styleId}` | `BannerController` | 다른 스타일 상세 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/moodboard-images` | `TasteController` | 무드보드 이미지 조회 |  |  |  |  |
| DEPRECATED_CANDIDATE | POST | `/api/v1/addresses` | `AddressController` | 주소 등록 |  | 프론트 API 함수 존재, Loki 72h/nginx 14d 0건 |  | 이전 버전 기능으로 삭제 후보 |
| DEPRECATED_CANDIDATE | GET | `/api/v1/house-templates` | `FloorPlanController` | 도면 템플릿 목록 조회 v1 |  |  | `/api/v2/house-templates` | 프론트 consumer 미확인, Loki 72h 0건 |
| ACTIVE | GET | `/api/v2/house-templates` | `FloorPlanController` | 도면 템플릿 목록 조회 v2 |  |  |  |  |
| ACTIVE | GET | `/api/v2/house-templates/{floorPlanId}` | `FloorPlanController` | 도면 템플릿 상세 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v2/recent-floor-plan` | `FloorPlanController` | 최근 도면 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/housing-options` | `HouseController` | 주거 옵션 조회 |  |  |  |  |
| ACTIVE | POST | `/api/v1/housing-selections` | `HouseController` | 주거 선택 저장 |  |  |  |  |
| DEPRECATED_CANDIDATE | GET | `/api/v1/carousels` | `CarouselController` | 캐러셀 목록 조회 v1 |  |  | `/api/v2/carousels` | 프론트 consumer 미확인, Loki 72h 0건 |
| ACTIVE | GET | `/api/v2/carousels` | `CarouselController` | 캐러셀 목록 조회 v2 |  |  |  |  |
| DEPRECATED_CANDIDATE | POST | `/api/v1/carousels/like` | `CarouselController` | 캐러셀 좋아요 v1 |  |  | `/api/v2/carousels/like` | 이전 버전 기능으로 삭제 후보 |
| DEPRECATED_CANDIDATE | POST | `/api/v1/carousels/hate` | `CarouselController` | 캐러셀 싫어요 v1 |  |  | `/api/v2/carousels/hate` | 프론트 consumer 미확인, Loki 72h 0건 |
| ACTIVE | POST | `/api/v2/carousels/like` | `CarouselController` | 캐러셀 좋아요 v2 |  |  |  |  |
| ACTIVE | POST | `/api/v2/carousels/hate` | `CarouselController` | 캐러셀 싫어요 v2 |  |  |  | 유지 필요 API, 프론트 consumer 미확인으로 FE 노티 필요 |
| ACTIVE | GET | `/api/v1/generated-images/list-result/{imageId}/items` | `GenerateImageResultController` | 생성 이미지 결과 상품 목록 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/generated-images/{imageId}/meta` | `GenerateImageResultController` | 생성 이미지 메타 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/generated-images/list-result/{imageId}/similar-items` | `GenerateImageResultController` | 생성 이미지 유사 상품 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/generated-images/list-result/{imageId}/related-images` | `GenerateImageResultController` | 생성 이미지 관련 이미지 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/factors` | `FactorController` | 선호 요인 목록 조회 |  |  |  | 사용 중, Loki 72h 1건/nginx 14d 소량 호출 |
| ACTIVE | POST | `/api/v1/generated-images/{imageId}/preference/factors/{factorId}` | `FactorController` | 생성 이미지 선호 요인 저장 |  |  |  | 사용 중, Loki 72h 1건/nginx 14d 소량 호출 |
| DEPRECATED_CANDIDATE | GET | `/api/v1/dashboard-info` | `FurnitureController` | 주요 활동 및 가구 목록 조회 v1 |  |  | `/api/v2/dashboard/activities`, `/api/v2/dashboard/categories` | 프론트 consumer 미확인, Loki 72h 0건 |
| ACTIVE | GET | `/api/v2/dashboard/activities` | `FurnitureController` | 주요 활동별 가구 매핑 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v2/dashboard/categories` | `FurnitureController` | 대시보드 가구 카테고리 조회 |  |  |  |  |
| DEPRECATED_CANDIDATE | GET | `/api/v1/generated-images/{imageId}/curations/categories` | `FurnitureController` | 생성 이미지 가구 카테고리 조회 v1 |  |  | `/api/v2/generated-images/{imageId}/curations/categories` | 프론트 consumer 미확인, Loki 72h 0건 |
| ACTIVE | GET | `/api/v2/generated-images/{imageId}/curations/categories` | `FurnitureController` | 생성 이미지 가구 카테고리 조회 v2 |  |  |  |  |
| ACTIVE | GET | `/api/v1/generated-images/{imageId}/curations/products/{categoryId}` | `FurnitureController` | 생성 이미지 기반 큐레이션 상품 조회 |  |  |  |  |
| DEPRECATED_CANDIDATE | GET | `/api/v1/generated-images/{tagId}/curations/products/{furnitureId}/for-plan` | `FurnitureController` | 기획 의사결정용 상품 조회 |  |  | `/api/v1/generated-images/{tagId}/curations/products/{furnitureId}/for-plan/detail` | Postman/수동 미사용, Loki 72h/nginx 14d 0건 |
| DEPRECATED_CANDIDATE | GET | `/api/v1/generated-images/{tagId}/curations/products/{furnitureId}/for-plan/detail` | `FurnitureController` | 기획 의사결정용 상품 조회 상세 |  |  |  | Postman/수동 미사용, Loki 72h/nginx 14d 0건 |
| DEPRECATED_CANDIDATE | POST | `/api/v1/credits/logs` | `PaymentBtnClickLogController` | 충전하기 버튼 클릭 로그 저장 |  |  |  | 프론트 consumer 미확인, Loki 72h 0건 |
| DEPRECATED_CANDIDATE | POST | `/api/v1/recommend-furnitures/{recommendFurnitureId}/jjym` | `JjymController` | 추천 가구 찜 토글 v1 |  |  | `/api/v2/curation-raw-products/{rawProductId}/jjym` | 프론트 consumer 미확인, Loki 72h 0건 |
| DEPRECATED_CANDIDATE | GET | `/api/v1/jjyms` | `JjymController` | 찜 목록 조회 v1 |  |  | `/api/v2/jjyms` | 프론트 런타임은 v2 사용 |
| ACTIVE | POST | `/api/v2/curation-raw-products/{rawProductId}/jjym` | `JjymController` | 원천 상품 찜 토글 v2 |  |  |  |  |
| ACTIVE | GET | `/api/v2/jjyms` | `JjymController` | 찜 목록 조회 v2 |  |  |  |  |
| DEPRECATED_CANDIDATE | POST | `/api/v2/image/generate` | `FastApiController` | FastAPI 이미지 생성 |  |  |  | 미사용, Loki 72h/nginx 14d 0건 |
| DEPRECATED_CANDIDATE | POST | `/api/v1/generated-images/generate` | `GenerateImageController` | Spring 기반 이미지 생성 v1 |  |  | `/api/v4/generated-images/generate` | 프론트 런타임은 v4/특화 생성 API 사용 |
| DEPRECATED_CANDIDATE | POST | `/api/v1/generated-images/generate/gemini` | `GenerateImageController` | Gemini 이미지 생성 v1 |  |  | `/api/v4/generated-images/generate` | 프론트 런타임은 v4/특화 생성 API 사용 |
| DEPRECATED_CANDIDATE | POST | `/api/v2/generated-images/generate` | `GenerateImageController` | FastAPI 기반 이미지 생성 v2 |  |  | `/api/v4/generated-images/generate` | 프론트 런타임은 v4/특화 생성 API 사용 |
| DEPRECATED_CANDIDATE | POST | `/api/v2/generated-images/generate/gemini` | `GenerateImageController` | Gemini 이미지 생성 v2 |  |  | `/api/v4/generated-images/generate` | 프론트 런타임은 v4/특화 생성 API 사용 |
| DEPRECATED_CANDIDATE | POST | `/api/v3/generated-images/generate` | `GenerateImageController` | 이미지 2장 생성 v3 |  |  | `/api/v4/generated-images/generate` | 프론트 런타임은 v4/특화 생성 API 사용 |
| DEPRECATED_CANDIDATE | POST | `/api/v3/generated-images/generate/gemini` | `GenerateImageController` | Gemini 이미지 2장 생성 v3 |  |  | `/api/v4/generated-images/generate` | 프론트 런타임은 v4/특화 생성 API 사용 |
| ACTIVE | POST | `/api/v4/generated-images/generate` | `GenerateImageController` | 이미지 생성 v4 |  |  |  |  |
| ACTIVE | POST | `/api/v1/generated-images/generate/banner` | `GenerateImageController` | 배너 템플릿 기반 이미지 생성 |  |  |  |  |
| ACTIVE | POST | `/api/v1/generated-images/generate/other-style` | `GenerateImageController` | 스타일 템플릿 기반 이미지 생성 |  |  |  |  |
| ACTIVE | POST | `/api/v1/generated-images/generate/products` | `GenerateImageController` | 선택 상품 기반 이미지 생성 |  |  |  |  |
| ACTIVE | GET | `/api/v1/generated-images/generate` | `GenerateImageController` | 이미지 생성 폴백 조회 |  |  |  | 유지 필요 폴백 API, Loki 72h/nginx 14d 0건 |
| ACTIVE | POST | `/api/v1/generated-images/{imageId}/preference` | `GenerateImageController` | 생성 이미지 선호 저장 |  |  |  |  |
| ACTIVE | DELETE | `/api/v1/generated-images/{imageId}/preference` | `GenerateImageController` | 생성 이미지 선호 삭제 |  |  |  |  |
| DEPRECATED_CANDIDATE | POST | `/api/v1/image/generate` | `OpenAiController` | OpenAI 이미지 생성 |  |  |  | 미사용, Loki 72h/nginx 14d 0건 |
| ACTIVE | GET | `/api/v2/curations/products` | `CurationProductV2Controller` | 큐레이션 상품 목록 조회 v2 |  |  |  |  |
| DEPRECATED_CANDIDATE | POST | `/api/v1/furnitures/logs` | `FurnitureRecommendBtnClickLogController` | 가구 추천 버튼 클릭 로그 저장 |  |  |  | 프론트 consumer 미확인, Loki 72h 0건 |
| DEPRECATED_CANDIDATE | GET | `/api/v1/curations/products` | `CurationProductController` | 큐레이션 상품 목록 조회 v1 |  |  | `/api/v2/curations/products` | 프론트 런타임은 v2 사용 |
| ACTIVE | GET | `/api/v1/curations/products/filters` | `CurationProductController` | 큐레이션 상품 필터 조회 |  |  |  |  |
| ACTIVE | GET | `/api/v1/curations/products/{id}` | `CurationProductController` | 큐레이션 상품 상세 조회 |  |  |  |  |
| DEPRECATED_CANDIDATE | GET | `/api/v1/mypage/user` | `UserController` | 마이페이지 사용자 정보 조회 v1 |  |  | `/api/v2/mypage/user` | 미사용으로 재분류, 삭제 전 v2 전환 확인 필요 |
| DEPRECATED_CANDIDATE | GET | `/api/v1/mypage/images` | `UserController` | 마이페이지 이미지 목록 조회 v1 |  |  | `/api/v2/mypage/images` | 프론트 런타임은 v2 사용 |
| DEPRECATED_CANDIDATE | GET | `/api/v1/mypage/images/{houseId}` | `UserController` | 마이페이지 이미지 상세 조회 |  |  |  | 프론트 consumer 미확인, Loki 72h 0건 |
| DEPRECATED_CANDIDATE | PATCH | `/api/v1/sign-up` | `UserController` | 회원가입 추가 정보 저장 v1 |  |  | `/api/v2/sign-up` | 프론트 consumer 미확인, Loki 72h 0건 |
| DEPRECATED_CANDIDATE | POST | `/api/v1/sign-up` | `UserController` | 회원가입 v1 |  |  | `/api/v2/sign-up` | 프론트 consumer 미확인, Loki 72h 0건 |
| ACTIVE | DELETE | `/api/v1/user` | `UserController` | 회원 탈퇴 |  |  |  |  |
| ACTIVE | GET | `/oauth/kakao` | `OAuthController` | Kakao OAuth 시작 |  |  |  |  |
| ACTIVE | GET | `/oauth/kakao/callback` | `OAuthController` | Kakao OAuth callback |  |  |  |  |
| ACTIVE | POST | `/logout` | `OAuthController` | 로그아웃 |  |  |  |  |
| DEPRECATED_CANDIDATE | GET | `/api/v1/check-has-generated-image` | `UserLandingController` | 생성 이미지 보유 여부 조회 |  |  |  | 프론트 consumer 미확인, Loki 72h 0건 |
| INTERNAL_ONLY | GET | `/access` | `JWTController` | access token 확인 |  |  |  | 개발자 테스트용 내부 API |
| INTERNAL_ONLY | GET | `/access-test` | `JWTController` | access token 테스트 |  |  |  | 개발자 테스트용 내부 API |
| ACTIVE | POST | `/reissue` | `JWTController` | token 재발급 |  |  |  |  |
| ACTIVE | PATCH | `/api/v2/sign-up` | `UserV2Controller` | 회원가입 추가 정보 저장 v2 |  |  |  |  |
| ACTIVE | POST | `/api/v2/sign-up` | `UserV2Controller` | 회원가입 v2 |  |  |  |  |
| ACTIVE | GET | `/api/v2/nickname/rotate` | `UserV2Controller` | 닉네임 랜덤 생성 |  |  |  |  |
| ACTIVE | GET | `/api/v2/mypage/user` | `UserV2Controller` | 마이페이지 사용자 정보 조회 v2 |  |  |  |  |
| ACTIVE | PATCH | `/api/v2/mypage/user` | `UserV2Controller` | 마이페이지 사용자 정보 수정 |  |  |  |  |
| ACTIVE | GET | `/api/v2/mypage/images` | `UserV2Controller` | 마이페이지 이미지 목록 조회 v2 |  |  |  |  |

## Admin APIs

서버 내부 SSR 어드민 대시보드인 `templates/admin/dashboard.html`에서 호출하는 API입니다.
일반 사용자 앱 API와 분리해 `INTERNAL_ONLY`로 분류합니다.

| Status | Method | Path | Controller | Purpose | Frontend Path | Other Source | Latest Alternative | Action |
|---|---|---|---|---|---|---|---|---|
| INTERNAL_ONLY | GET | `/api/v1/admin/curation-raw-products` | `AdminCurationRawProductController` | 원천 큐레이션 상품 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/curation-raw-products/color-options` | `AdminCurationRawProductController` | 원천 상품 색상 옵션 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/curation-raw-products/{curationRawProductId}` | `AdminCurationRawProductController` | 원천 큐레이션 상품 상세 조회 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/curation-raw-products` | `AdminCurationRawProductController` | 원천 큐레이션 상품 생성 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/curation-raw-products/{curationRawProductId}` | `AdminCurationRawProductController` | 원천 큐레이션 상품 수정 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/curation-raw-products/exposure` | `AdminCurationRawProductController` | 원천 큐레이션 상품 노출 상태 변경 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/curation-raw-products/{curationRawProductId}/furniture-tags` | `AdminCurationRawProductController` | 원천 상품 가구 태그 매핑 생성 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/curation-raw-products/{curationRawProductId}/furniture-tags/{mappingId}` | `AdminCurationRawProductController` | 원천 상품 가구 태그 매핑 수정 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/curation-raw-products/{curationRawProductId}/furniture-tags/{mappingId}` | `AdminCurationRawProductController` | 원천 상품 가구 태그 매핑 삭제 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/curation-raw-products/{curationRawProductId}` | `AdminCurationRawProductController` | 원천 큐레이션 상품 삭제 |  |  |  |  |
| UNKNOWN | POST | `/api/v1/admin/curations/soozip/raw` | `AdminSoozipRawProductController` | Soozip 원천 상품 동기화 |  |  |  | dashboard.html 호출 근거 미확인 |
| INTERNAL_ONLY | POST | `/api/v1/admin/banners/image-upload-url` | `AdminBannerController` | 배너 이미지 업로드 URL 생성 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/banners` | `AdminBannerController` | 배너 생성 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/banners` | `AdminBannerController` | 배너 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/banners/{bannerId}` | `AdminBannerController` | 배너 상세 조회 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/banners/{bannerId}` | `AdminBannerController` | 배너 수정 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/banners/{bannerId}` | `AdminBannerController` | 배너 삭제 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/banners/raw-products/search` | `AdminBannerController` | 배너용 원천 상품 검색 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/landings/image-upload-url` | `AdminLandingController` | 랜딩 이미지 업로드 URL 생성 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/landings` | `AdminLandingController` | 랜딩 생성 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/landings` | `AdminLandingController` | 랜딩 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/landings/{landingId}` | `AdminLandingController` | 랜딩 상세 조회 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/landings/{landingId}` | `AdminLandingController` | 랜딩 수정 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/landings/{landingId}` | `AdminLandingController` | 랜딩 삭제 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/members` | `AdminMemberController` | 어드민 회원 검색 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/members/{memberId}/credits` | `AdminMemberController` | 회원 크레딧 지급 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/floor-plans/image-upload-url` | `AdminFloorPlanController` | 도면 이미지 업로드 URL 생성 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/floor-plans` | `AdminFloorPlanController` | 도면 생성 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/floor-plans` | `AdminFloorPlanController` | 도면 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/floor-plans/{floorPlanId}` | `AdminFloorPlanController` | 도면 상세 조회 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/floor-plans/{floorPlanId}` | `AdminFloorPlanController` | 도면 수정 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/floor-plans/{floorPlanId}` | `AdminFloorPlanController` | 도면 삭제 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/moodboard` | `AdminMoodBoardController` | 무드보드 등록 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/moodboards` | `AdminMoodBoardController` | 무드보드 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/moodboard` | `AdminMoodBoardController` | 무드보드 삭제 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/styles/image-upload-url` | `AdminStyleController` | 스타일 이미지 업로드 URL 생성 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/styles` | `AdminStyleController` | 스타일 생성 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/styles` | `AdminStyleController` | 스타일 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/styles/{styleId}` | `AdminStyleController` | 스타일 상세 조회 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/styles/{styleId}` | `AdminStyleController` | 스타일 수정 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/styles/{styleId}` | `AdminStyleController` | 스타일 삭제 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/styles/raw-products/search` | `AdminStyleController` | 스타일용 원천 상품 검색 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/furniture` | `AdminFurnitureController` | 가구 등록 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/furniture/type` | `AdminFurnitureController` | 가구 타입 등록 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/furniture/prompt` | `AdminFurnitureController` | 가구 프롬프트 등록 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/furnitures` | `AdminFurnitureController` | 가구 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/furniture/types` | `AdminFurnitureController` | 가구 타입 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/furniture/tags` | `AdminFurnitureController` | 가구 태그 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/furniture/types/{furnitureTypeId}/tags` | `AdminFurnitureController` | 가구 타입별 태그 조회 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/furniture/types/{furnitureTypeId}/furnitures` | `AdminFurnitureController` | 가구 타입별 가구 조회 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/furniture` | `AdminFurnitureController` | 가구 수정 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/furniture/type` | `AdminFurnitureController` | 가구 타입 수정 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/furniture/tag` | `AdminFurnitureController` | 가구 태그 삭제 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/furniture` | `AdminFurnitureController` | 가구 삭제 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/furniture/type` | `AdminFurnitureController` | 가구 타입 삭제 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/furniture/prompt` | `AdminFurnitureController` | 가구 프롬프트 조회 |  |  |  |  |
| INTERNAL_ONLY | POST | `/api/v1/admin/tag` | `AdminTagController` | 태그 등록 |  |  |  |  |
| INTERNAL_ONLY | GET | `/api/v1/admin/tags` | `AdminTagController` | 태그 목록 조회 |  |  |  |  |
| INTERNAL_ONLY | PATCH | `/api/v1/admin/tag` | `AdminTagController` | 태그 수정 |  |  |  |  |
| INTERNAL_ONLY | DELETE | `/api/v1/admin/tag` | `AdminTagController` | 태그 삭제 |  |  |  |  |
| UNKNOWN | GET | `/api/v1/admin/test` | `AdminController` | 어드민 권한 테스트 |  |  |  | dashboard.html 호출 근거 미확인 |

## Admin SSR Routes

| Status | Method | Path | Controller | Purpose | Frontend Path | Other Source | Latest Alternative | Action |
|---|---|---|---|---|---|---|---|---|
| INTERNAL_ONLY | GET | `/admin/login` | `AdminSSRController` | 어드민 로그인 화면 |  |  |  |  |
| INTERNAL_ONLY | POST | `/admin/login` | `AdminSSRController` | 어드민 로그인 처리 |  |  |  |  |
| INTERNAL_ONLY | GET | `/admin/dashboard` | `AdminSSRController` | 어드민 대시보드 화면 |  |  |  |  |
