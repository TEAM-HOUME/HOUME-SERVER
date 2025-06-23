package or.sopt.houme.global.error.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    REQUEST_HEADER_EMPTY(HttpStatus.BAD_REQUEST, 40006, "요청 헤더가 누락되었습니다."),

    NOT_FOUND_URL(HttpStatus.NOT_FOUND, 40401, "지원하지 않는 URL입니다."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 40501, "잘못된 HTTP method 요청입니다."),
    ;

    private final HttpStatus status;
    private final int code;
    private final String msg;

    ErrorCode(HttpStatus status, int code, String msg) {
        this.status = status;
        this.code = code;
        this.msg = msg;
    }
}
