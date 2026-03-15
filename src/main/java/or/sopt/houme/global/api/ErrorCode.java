package or.sopt.houme.global.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    /**
     * 400 BAD_REQUEST
     */
    // 인증관련
    REQUEST_HEADER_EMPTY(HttpStatus.BAD_REQUEST, 40000, "요청 헤더가 누락되었습니다."),

    // DB관련
    FOREIGN_KEY_CONSTRAINT_FAIL(HttpStatus.BAD_REQUEST,40010 ,"연관된 데이터들을 먼저 삭제해주세요" ),

    // Token 관련 예외
    ACCESS_INVALID_TYPE(HttpStatus.BAD_REQUEST,40001 ,"액세스 토큰이 존재하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST,40002 ,"액세스 토큰이 만료되었습니다." ),
    REFRESH_TOKEN_NULL(HttpStatus.BAD_REQUEST,40003 ,"리프레시 토큰이 존재하지 않습니다"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST,40004 ,"리프레시 토큰이 만료되었습니다" ),
    ROLE_INVALID_TYPE(HttpStatus.BAD_REQUEST,40005,"권한이 일치하지 않습니다"),

    // 소셜 예외
    KAKAO_AUTH_CODE_INVALID(HttpStatus.BAD_REQUEST,40006 ,"카카오 인가코드가 유효하지 않습니다"),

    // HouseEnum 처리 예외
    HOUSE_NOT_ALLOWED_OPTION(HttpStatus.BAD_REQUEST, 40007, "유효하지 않은 집 구조 옵션입니다."),

    // 입력값 검증 예외
    NOT_VALID_EXCEPTION(HttpStatus.BAD_REQUEST, 40008, "유효하지 않은 입력값입니다."),

    // 회원관련
    USERNAME_DUPLICATE(HttpStatus.BAD_REQUEST,40009,"username이 중복되었습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 40021, "이미 가입된 이메일입니다."),
    DUPLICATE_CURATION_RAW_PRODUCT(HttpStatus.BAD_REQUEST, 40022, "이미 동일한 원본 상품(source/category/product_id)이 존재합니다."),
    DUPLICATE_CURATION_RAW_PRODUCT_FURNITURE_TAG(HttpStatus.BAD_REQUEST, 40023, "이미 동일한 원본 상품 가구 태그 매핑이 존재합니다."),

    // 소셜 회원가입 관련
    SIGNUP_TOKEN_INVALID(HttpStatus.BAD_REQUEST, 40020, "회원가입 토큰이 유효하지 않습니다."),

    // 좋아요 관련
    MISMATCHED_IS_LIKE(HttpStatus.BAD_REQUEST,40011 ,"좋아요와 요인의 선호 여부가 일치하지 않습니다."),

    // 이미지 관련 예외
    IMAGE_PROCESSING_ERROR(HttpStatus.BAD_REQUEST,40012 ,"이미지 컬러 해시값 계산 중 오류가 발생하였습니다."),

    // 네이버 쇼핑 API 관련 예외
    NAVER_API_DATA_PARSE_ERROR(HttpStatus.BAD_REQUEST, 40013, "네이버 API 응답의 productId 필드를 숫자로 변환하는 중 오류가 발생했습니다."),
    NAVER_API_CLIENT_ERROR(HttpStatus.BAD_REQUEST, 40014, "네이버 API 요청이 잘못되었습니다."),

    // Fast API imageHash 관련 예외
    IMAGE_HASH_CLIENT_ERROR(HttpStatus.BAD_REQUEST, 40015, "Fast API 요청이 잘못되었습니다."),

    // 클라이언트가 JSON body를 잘못 보냈을 때
    REQUEST_BODY_NOT_READABLE(HttpStatus.BAD_REQUEST, 40016, "요청 데이터 타입이 일치하지 않습니다."),

    // 어드민 FurnitureType 관련 예외
    DUPLICATE_FURNITURE_TYPE_KR(HttpStatus.BAD_REQUEST, 40017, "중복된 가구타입 한글명입니다."),
    DUPLICATE_FURNITURE_TYPE_ENG(HttpStatus.BAD_REQUEST, 40018, "중복된 가구타입 영어명입니다."),
    CANNOT_DELETE_FURNITURE_TYPE_IN_USE(HttpStatus.BAD_REQUEST, 40019, "해당 가구 타입을 사용하는 가구들을 먼저 삭제해주세요."),


    /**
     * 401 UNAUTHORIZED
     */
    // Token 관련 예외
    ACCESS_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED,40101 ,"회원의 액세스 토큰이 블랙리스트 처리되었습니다" ),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED,40102,"토큰의 서명이 유효하지 않습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,40103,"유효하지 않은 토큰입니다"),

    /**
     * 403 FORBIDDEN
     */
    USER_ROLE_EXCEPTION(HttpStatus.FORBIDDEN,40300 ,"회원의 권한을 찾을 수 없습니다. 서버에 문의해주세요" ),

    /**
     * 404 NOT_FOUND
     */
    NOT_FOUND_URL(HttpStatus.NOT_FOUND, 40400, "지원하지 않는 URL입니다."),

    // 회원 관련 예외
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,40401 ,"회원을 찾을 수 없습니다" ),
    COOKIE_NULL(HttpStatus.NOT_FOUND,40402 ,"쿠키를 찾을 수 없습니다" ),
    IMAGE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND,40403 ,"이미지 생성 이력을 찾을 수 없습니다." ),

    // 도면 관련 예외
    NOT_FOUND_HOUSE(HttpStatus.NOT_FOUND, 40404, "저장되어있는 주거 정보가 없습니다."),

    // 가구 관련 예외
    NOT_FOUND_FURNITURE(HttpStatus.NOT_FOUND,40413, "가구 객체를 찾을 수 없습니다"),
    NOT_FOUND_FURNITURE_TAG(HttpStatus.NOT_FOUND,40414,"가구 태그 객체를 찾을 수 없습니다"),
    INVALID_DELETE_FURNITURE(HttpStatus.BAD_REQUEST,40415,"태그가 있는 가구가 존재하여 가구를 삭제 할 수 없습니다"),
    ALREADY_EXIST_FURNITURE(HttpStatus.BAD_REQUEST,40416,"이미 동명의 가구가 존재하여 추가 할 수 없습니다"),

    // 집 엔티티 관련 예외
    NOT_FOUND_HOUSE_ENTITY(HttpStatus.NOT_FOUND, 40405, "집 객체를 찾을 수 없습니다." ),

    // 태그 엔티티 관련 예외
    NOT_FOUND_TAG_ENTITY(HttpStatus.NOT_FOUND, 40406, "태그 객체를 찾을 수 없습니다." ),
    ALREADY_EXIST_TAG(HttpStatus.BAD_REQUEST,40420, "이미 존재하는 태그입니다"),
    ALREADY_EXIST_PRIORITY(HttpStatus.BAD_REQUEST,40417,"이미 존재하는 우선순위 입니다"),

    // 생성된 이미지 엔티티 관련 예외
    NOT_FOUND_GENERATE_IMAGE_ENTITY(HttpStatus.NOT_FOUND, 40407, "생성된 이미지 객체를 찾을 수 없습니다." ),

    // 캐러셀 관련 예외
    CAROUSEL_NOT_FOUND(HttpStatus.NOT_FOUND,40408,"캐러셀을 찾을 수 없습니다"),
    CAROUSEL_PREFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND,40409,"캐러셀 선호도 레코드를 찾을 수 없습니다"),

    // 도면 관련 예외
    NOT_FOUND_FLOOR_PLAN(HttpStatus.NOT_FOUND, 40410, "도면을 찾을 수 없습니다."),

    // 크레딧 관련 예외
    CREDIT_NOT_FOUND(HttpStatus.NOT_FOUND, 40411, "크레딧을 찾을 수 없습니다."),

    // 무드보드 (taste)
    NOT_FOUND_TASTE(HttpStatus.NOT_FOUND, 40412, "제공된 무드보드 ID에 해당하는 취향을 찾을 수 없습니다."),

    // 좋아요 관련 예외
    NOT_FOUND_PREFERENCE(HttpStatus.NOT_FOUND, 40418, "유저와 이미지에 매핑되는 좋아요를 찾을 수 없습니다."),
    NOT_FOUND_FACTOR(HttpStatus.NOT_FOUND, 40419, "해당 요인을 찾을 수 없습니다."),

    // Admin 가구 타입 관련 예외
    NOT_FOUND_FURNITURE_TYPE(HttpStatus.NOT_FOUND, 40421, "가구 타입을 찾을 수 없습니다."),
    NAVER_RESPONSE_EMPTY(HttpStatus.NOT_FOUND,40422,"네이버 API 응답 결과가 없습니다."),

    // House_taste
    NOT_FOUND_HOUSE_TASTE(HttpStatus.NOT_FOUND, 40423, "집 취향을 찾을 수 없습니다."),
    NOT_FOUND_CURATION_RAW_PRODUCT(HttpStatus.NOT_FOUND, 40424, "원본 상품 객체를 찾을 수 없습니다."),
    NOT_FOUND_CURATION_RAW_PRODUCT_FURNITURE_TAG_MAPPING(HttpStatus.NOT_FOUND, 40425, "원본 상품 가구 태그 매핑을 찾을 수 없습니다."),
    NOT_FOUND_BANNER(HttpStatus.NOT_FOUND, 40426, "배너 객체를 찾을 수 없습니다."),
    NOT_FOUND_STYLE(HttpStatus.NOT_FOUND, 40427, "스타일 객체를 찾을 수 없습니다."),


    /**
     * 405 METHOD_NOT_ALLOWED
     */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 40500, "잘못된 HTTP method 요청입니다."),

    /**
     * 409 CONFLICT
     */
    CREDIT_LOCK_FAILED(HttpStatus.CONFLICT, 40900, "다른 요청을 처리 중입니다. 잠시 후 다시 시도해주세요."),

    /**
     * 429 Too_Many_Requests
     */
    RETRY_GET_IMAGE(HttpStatus.TOO_MANY_REQUESTS, 42900, "너무 많은 요청이 들어왔습니다. 잠시 후에 재요청하세요."),
    ASYNC_POOL_OVERFLOW(HttpStatus.TOO_MANY_REQUESTS, 42901, "현재 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    /**
     * 500 INTERNAL_SERVER_ERROR
     */
    KAKAO_ACCESSTOKEN_INVALID(HttpStatus.INTERNAL_SERVER_ERROR,50001 ,"액세스토큰으로 회원정보를 가져오는 중에 예외가 발생했습니다. 서버개발자에게 문의해주세요" ),

    // 이미지 관련 예외
    IMAGE_UPLOAD_AMAZON_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 50002, "이미지 업로드 중, AWS 예외가 발생하였습니다. 서버 관리자에게 문의해주세요"),
    IMAGE_UPLOAD_IO_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 50003, "이미지 업로드 중, IO 예외가 발생하였습니다. 서버 관리자에게 문의해주세요"),
    IMAGE_STILL_EXIST(HttpStatus.INTERNAL_SERVER_ERROR,50005 ,"이미지가 삭제되지 않고 S3에 남아있습니다. 서버 관리자에게 문의해주세요" ),
    IMAGE_DELETE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50006 ,"이미지 삭제에 실패하였습니다. 서버관리자에게 문의해주세요" ),
    HTTP_MEDIA_TYPE_NOT_ACCEPTABLE(HttpStatus.INTERNAL_SERVER_ERROR,50007,"HTTP 리퀘스트 타입에 오류가 발생하였습니다"),
    INCODING_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50008 ,"인코딩 과정 중 예외가 발생하였습니다" ),
    CHAT_GPT_CALL_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50009 ,"챗 gpt 호출 중 예외가 발생하였습니다" ),

    // 캐러셀 관련 예외
    CAROUSEL_RETRY_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50010,"캐러셀 좋아요,싫어요 시도 중 동시성 예외가 발생하였습니다, 서버 개발자에게 문의해주세요"),
    CAROUSEL_INTERRUPT_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50011,"캐러셀 API 실행 중, INTERRUPT_EXCEPTION 가 발생하였습니다. 서버 개발자에게 문의해주세요"),

    // 크레딧 생성 관련 예외
    CREDIT_CREATE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50012,"크레딧 생성 과정 중 예외가 발생하였습니다. 서버 관리자에게 문의해주세요"),

    // 이미지 생성 중
    GENERATED_IMAGE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 50013, "이미지 생성 중 예외가 발생하였습니다. 서버 관리자에게 문의해주세요"),
    CREDIT_LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, 50014, "크레딧 처리 대기 중 스레드 인터럽트가 발생했습니다. 서버 관리자에게 문의해주세요"),

    // DB 관련
    DATA_INTEGRITY_VIOLATION(HttpStatus.INTERNAL_SERVER_ERROR,50015, "데이터 무결성 제약 조건을 위반했습니다. 서버 관리자에게 문의해주세요"),

    // GenerateImage 좋아요 | 생성된 이미지 관련 예외
    GENERATE_IMAGE_RETRY_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50016,"생성된 이미지 좋아요,싫어요 시도 중 동시성 예외가 발생하였습니다, 서버 개발자에게 문의해주세요"),
    GENERATE_IMAGE_INTERRUPT_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50017,"이미지 API 실행 중, INTERRUPT_EXCEPTION 가 발생하였습니다. 서버 개발자에게 문의해주세요"),

    // DB 제약조건 위반 에러
    DB_CONSTRAINT_VIOLATION(HttpStatus.INTERNAL_SERVER_ERROR, 50018, "DB 제약조건 문제 발생, 서버 개발자에게 문의해주세요"),

    // 로그 저장시 objectMapper 에러
    OBJECTMAPPER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 50019, "생성 로그 JSON 변환에 실패, 서버 개발자에게 문의해주세요"),
    NICKNAME_RESOURCE_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, 50020, "닉네임 리소스가 비어 있습니다. 서버 관리자에게 문의해주세요"),

    /**
     * 502 BAD_GATEWAY
     */
    // 네이버 쇼핑 API 관련
    NAVER_API_SERVER_ERROR(HttpStatus.BAD_GATEWAY, 50200, "네이버 API 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    NAVER_API_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, 50201, "네이버 API 응답이 비어 있습니다."),

    // FastAPI imageHash 관련
    IMAGE_HASH_SERVER_ERROR(HttpStatus.BAD_GATEWAY, 50202, "Fast API 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    IMAGE_HASH_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, 50203, "Fast API imageHash 응답이 비어 있습니다."),


    /**
     * 504 GATEWAY_TIMEOUT
     */
    // 이미지 생성 타임아웃
    GENERATED_IMAGE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, 50400, "이미지 생성 시간이 초과되었습니다.");

    private final HttpStatus status;
    private final int code;
    private final String msg;

    ErrorCode(HttpStatus status, int code, String msg) {
        this.status = status;
        this.code = code;
        this.msg = msg;
    }
}
