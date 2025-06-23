package or.sopt.houme.global.error.exception;

import lombok.Getter;
import or.sopt.houme.global.error.code.ErrorCode;

@Getter
public class BusinessException extends RuntimeException {private final ErrorCode errorCode;
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
