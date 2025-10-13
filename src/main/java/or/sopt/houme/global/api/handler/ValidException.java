package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class ValidException extends GeneralException {
    public ValidException(ErrorCode errorCode) {
        super(errorCode);
    }
}
