package or.sopt.houme.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int code, String msg, T data) {
    public static <T> ApiResponse<T> ok(T data, String msg) {
        return new ApiResponse<T>(HttpStatus.OK.value(), msg, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<T>(HttpStatus.CREATED.value(), "응답 성공", data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "응답 성공");
    }

    public static <T> ApiResponse<T> fail(int code, String msg) {
        return new ApiResponse<T>(code, msg, null);
    }
}
