package or.sopt.houme.global.api;

import lombok.Getter;

@Getter
public class BusinessException extends GeneralException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
