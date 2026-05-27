package or.sopt.houme.global.api;

import io.sentry.Sentry;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.handler.ImageFallbackException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.concurrent.RejectedExecutionException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    // Fallback 이미지 반환 Exception Handler
    @ExceptionHandler(ImageFallbackException.class)
    public ResponseEntity<ApiResponse<Object>> handleImageFallbackException(ImageFallbackException e) {
        // 센트리 알림
        Sentry.captureException(e);

        ErrorCode errorCode = e.getErrorCode();

        // Exception에서 imageInfo 추출 (ImageInfoList or ImageInfo)Response
        Object imageInfo = e.getImageInfo();

        // ApiResponse에 담기 (가독성을 위한 분리)
        ApiResponse<Object> response = ApiResponse.fail(
                errorCode.getCode(),
                imageInfo,
                "이미지 생성 중 예외가 발생하였습니다"
        );

        logException("exception.image_fallback", errorCode, e, true);
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);

        logException("exception.general", errorCode, e, isServerError(errorCode));
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }


    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
        ErrorCode errorCode = ErrorCode.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);

        logException("exception.http_media_type_not_acceptable", errorCode, e, false);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }


    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        ErrorCode errorCode = ErrorCode.REQUEST_HEADER_EMPTY;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(ex);

        logException("exception.missing_header", errorCode, ex, false);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);

        logException("exception.method_not_allowed", errorCode, e, false);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);

        logException("exception.type_mismatch", errorCode, e, false);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        ErrorCode errorCode = ErrorCode.NOT_FOUND_URL;

        Sentry.captureException(ex);

        logException("exception.not_found_url", errorCode, ex, false);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.NOT_VALID_EXCEPTION;

        Sentry.captureException(e);

        logException("exception.validation", errorCode, e, false);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodValidationException(HandlerMethodValidationException e) {
        ErrorCode errorCode = ErrorCode.NOT_VALID_EXCEPTION;

        Sentry.captureException(e);

        logException("exception.method_validation", errorCode, e, false);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        ErrorCode errorCode = ErrorCode.NOT_VALID_EXCEPTION;

        Sentry.captureException(e);

        logException("exception.constraint_violation", errorCode, e, false);
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e) {

        Sentry.captureException(e);

        logUnhandledException(e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류입니다."));
    }

    // 클라이언트가 JSON body를 잘못 보냈을 때 Valid로 안잡힌 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ErrorCode errorCode = ErrorCode.REQUEST_BODY_NOT_READABLE;

        Sentry.captureException(e);

        logException("exception.request_body_not_readable", errorCode, e, false);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    // DB 제약조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        ErrorCode errorCode = ErrorCode.DB_CONSTRAINT_VIOLATION;

        Sentry.captureException(e);

        logException("exception.db_constraint", errorCode, e, true);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    // 비동기 요청에서 스레드 풀 크기를 넘어선 경우 예외 발생
    @ExceptionHandler(RejectedExecutionException.class)
    public ResponseEntity<ApiResponse<Void>> handleRejected(RejectedExecutionException e){
        ErrorCode errorCode = ErrorCode.ASYNC_POOL_OVERFLOW;

        Sentry.captureException(e);

        logException("exception.async_pool_overflow", errorCode, e, true);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMsg()));
    }

    private void logException(String event, ErrorCode errorCode, Exception e, boolean withStackTrace) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = attributes == null ? "N/A" : attributes.getRequest().getMethod();
        String uri = attributes == null ? "N/A" : attributes.getRequest().getRequestURI();

        if (withStackTrace) {
            log.error(
                    "event={} method={} uri={} status={} errorCode={} exceptionType={} message={}",
                    event,
                    method,
                    uri,
                    errorCode.getStatus().value(),
                    errorCode.getCode(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e
            );
            return;
        }
        log.warn(
                "event={} method={} uri={} status={} errorCode={} exceptionType={} message={}",
                event,
                method,
                uri,
                errorCode.getStatus().value(),
                errorCode.getCode(),
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    private void logUnhandledException(Exception e) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = attributes == null ? "N/A" : attributes.getRequest().getMethod();
        String uri = attributes == null ? "N/A" : attributes.getRequest().getRequestURI();

        log.error(
                "event=exception.unhandled method={} uri={} status={} exceptionType={} message={}",
                method,
                uri,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                e
        );
    }

    private boolean isServerError(ErrorCode errorCode) {
        return errorCode.getStatus().is5xxServerError();
    }
}
