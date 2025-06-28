package or.sopt.houme.global.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * GeneralException이 발생했을 때 해당 에러 코드와 메시지를 포함한 실패 응답을 반환합니다.
     *
     * @param e 처리할 GeneralException 인스턴스
     * @return 에러 코드, 메시지, HTTP 상태가 포함된 ApiResponse를 담은 ResponseEntity
     */
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }


    /**
     * 요청 헤더가 누락된 경우 표준화된 에러 응답을 반환합니다.
     *
     * @param ex 누락된 요청 헤더 예외
     * @return 에러 코드와 메시지가 포함된 ApiResponse를 HTTP 상태와 함께 반환
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        ErrorCode errorCode = ErrorCode.REQUEST_HEADER_EMPTY;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    /**
     * 지원되지 않는 HTTP 요청 메서드가 사용된 경우 표준화된 오류 응답을 반환합니다.
     *
     * @param e 지원되지 않는 HTTP 메서드 예외
     * @return METHOD_NOT_ALLOWED 오류 코드와 메시지를 포함한 ApiResponse를 반환합니다.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND_URL;
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류입니다."));
    }
}
