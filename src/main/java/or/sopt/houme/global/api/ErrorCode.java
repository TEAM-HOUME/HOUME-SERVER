package or.sopt.houme.global.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    REQUEST_HEADER_EMPTY(HttpStatus.BAD_REQUEST, 40000, "요청 헤더가 누락되었습니다."),
    NOT_FOUND_URL(HttpStatus.NOT_FOUND, 40400, "지원하지 않는 URL입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 40500, "잘못된 HTTP method 요청입니다."),

    // Token 관련 예외
    ACCESS_INVALID_TYPE(HttpStatus.BAD_REQUEST,40001 ,"액세스 토큰이 존재하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST,40002 ,"액세스 토큰이 만료되었습니다." ),
    REFRESH_TOKEN_NULL(HttpStatus.BAD_REQUEST,40003 ,"리프레시 토큰이 존재하지 않습니다"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST,40004 ,"리프레시 토큰이 만료되었습니다" ),
    ROLE_INVALID_TYPE(HttpStatus.BAD_REQUEST,40005,"권한이 일치하지 않습니다"),
    ACCESS_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED,40401 ,"회원의 액세스 토큰이 블랙리스트 처리되었습니다" ),

    // HouseEnum 처리 예외
    HOUSE_NOT_ALLOWED_OPTION(HttpStatus.BAD_REQUEST, 40007, "유효하지 않은 집 구조 옵션입니다."),

    // 회원 관련 예외
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,40401 ,"회원을 찾을 수 없습니다" ),
    COOKIE_NULL(HttpStatus.NOT_FOUND,40402 ,"쿠키를 찾을 수 없습니다" ),
    KAKAO_AUTH_CODE_INVALID(HttpStatus.BAD_REQUEST,40006 ,"카카오 인가코드가 유효하지 않습니다"),
    KAKAO_ACCESSTOKEN_INVALID(HttpStatus.INTERNAL_SERVER_ERROR,50001 ,"액세스토큰으로 회원정보를 가져오는 중에 예외가 발생했습니다. 서버개발자에게 문의해주세요" ),
    USER_ROLE_EXCEPTION(HttpStatus.UNAUTHORIZED,40300 ,"회원의 권한을 찾을 수 없습니다. 서버에 문의해주세요" ),

    IMAGE_UPLOAD_AMAZON_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 50002, "이미지 업로드 중, AWS 예외가 발생하였습니다. 서버 관리자에게 문의해주세요"),
    IMAGE_UPLOAD_IO_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, 50003, "이미지 업로드 중, IO 예외가 발생하였습니다. 서버 관리자에게 문의해주세요"),
    IMAGE_STILL_EXIST(HttpStatus.INTERNAL_SERVER_ERROR,50005 ,"이미지가 삭제되지 않고 S3에 남아있습니다. 서버 관리자에게 문의해주세요" ),
    IMAGE_DELETE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,50006 ,"이미지 삭제에 실패하였습니다. 서버관리자에게 문의해주세요" );

    private final HttpStatus status;
    private final int code;
    private final String msg;

    ErrorCode(HttpStatus status, int code, String msg) {
        this.status = status;
        this.code = code;
        this.msg = msg;
    }
}
