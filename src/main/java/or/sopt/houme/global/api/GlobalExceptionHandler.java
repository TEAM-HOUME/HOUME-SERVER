package or.sopt.houme.global.api;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.global.api.handler.ImageFallbackException;
import or.sopt.houme.global.discord.ErrorAlertNotifier;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.concurrent.RejectedExecutionException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    // @WebMvcTest 등 슬라이스 테스트에는 이 빈이 없으므로 ObjectProvider 로 선택적 주입한다
    private final ObjectProvider<ErrorAlertNotifier> errorAlertNotifier;

    // Fallback 이미지 반환 Exception Handler
    @ExceptionHandler(ImageFallbackException.class)
    public ResponseEntity<ApiResponse<Object>> handleImageFallbackException(ImageFallbackException e) {
        // 센트리 알림
        Sentry.captureException(e);
        errorAlertNotifier.ifAvailable(notifier -> notifier.notifyServerError(e));

        ErrorCode errorCode = e.getErrorCode();

        // Exception에서 imageInfo 추출 (ImageInfoList or ImageInfo)Response
        Object imageInfo = e.getImageInfo();

        // ApiResponse에 담기 (가독성을 위한 분리)
        ApiResponse<Object> response = ApiResponse.fail(
                errorCode.getCode(),
                imageInfo,
                "이미지 생성 중 예외가 발생하였습니다"
        );

        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);
        // 5xx 계열만 디스코드 알림 (4xx/도메인 검증 실패는 노이즈라 제외)
        if (errorCode.getStatus().is5xxServerError()) {
            errorAlertNotifier.ifAvailable(notifier -> notifier.notifyServerError(e));
        }

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }


    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
        ErrorCode errorCode = ErrorCode.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }


    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        ErrorCode errorCode = ErrorCode.REQUEST_HEADER_EMPTY;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(ex);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND_URL;

        Sentry.captureException(ex);

        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    // 정적 리소스 없음(예: /swagger-ui/) — 실제 404 이므로 NoHandlerFound 와 동일하게 처리하고 5xx 알림을 보내지 않는다
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND_URL;

        Sentry.captureException(ex);

        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.NOT_VALID_EXCEPTION;

        Sentry.captureException(e);

        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodValidationException(HandlerMethodValidationException e) {
        ErrorCode errorCode = ErrorCode.NOT_VALID_EXCEPTION;

        Sentry.captureException(e);

        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        ErrorCode errorCode = ErrorCode.NOT_VALID_EXCEPTION;

        Sentry.captureException(e);

        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e) {

        Sentry.captureException(e);
        errorAlertNotifier.ifAvailable(notifier -> notifier.notifyServerError(e));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류입니다."));
    }

    // 클라이언트가 JSON body를 잘못 보냈을 때 Valid로 안잡힌 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ErrorCode errorCode = ErrorCode.REQUEST_BODY_NOT_READABLE;

        Sentry.captureException(e);

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    // DB 제약조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        ErrorCode errorCode = ErrorCode.DB_CONSTRAINT_VIOLATION;

        Sentry.captureException(e);
        errorAlertNotifier.ifAvailable(notifier -> notifier.notifyServerError(e));

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    // 비동기 요청에서 스레드 풀 크기를 넘어선 경우 예외 발생
    @ExceptionHandler(RejectedExecutionException.class)
    public ResponseEntity<ApiResponse<Void>> handleRejected(RejectedExecutionException e){
        ErrorCode errorCode = ErrorCode.ASYNC_POOL_OVERFLOW;

        Sentry.captureException(e);
        errorAlertNotifier.ifAvailable(notifier -> notifier.notifyServerError(e));

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }
}
