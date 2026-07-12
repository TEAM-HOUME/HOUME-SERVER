package or.sopt.houme.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int code, String msg, T data, String traceId) {

    private static final String TRACE_ID_MDC_KEY = "traceId";

    public ApiResponse(int code, String msg, T data) {
        this(code, msg, data, null);
    }

    public static <T> ApiResponse<T> ok(T data, String msg) {
        return new ApiResponse<T>(HttpStatus.OK.value(), msg, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<T>(HttpStatus.CREATED.value(), "응답 성공", data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "응답 성공");
    }

    // 에러 응답에만 traceId 를 포함한다 (NON_NULL 직렬화라 성공 응답 포맷은 불변)
    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<T>(code, msg, null, MDC.get(TRACE_ID_MDC_KEY));
    }

    public static <T> ApiResponse<T> fail(int code, T data, String msg) {
        return new ApiResponse<T>(code, msg, data, MDC.get(TRACE_ID_MDC_KEY));
    }
}
