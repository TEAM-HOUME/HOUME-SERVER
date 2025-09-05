package or.sopt.houme.global.api;

import io.sentry.Sentry;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoListResponse;
import or.sopt.houme.global.api.handler.ImageFallbackException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
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

        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());

        Sentry.captureException(e);

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e) {

        Sentry.captureException(e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류입니다."));
    }
}
