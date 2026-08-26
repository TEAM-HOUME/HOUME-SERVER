package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class CompareException extends GeneralException {
    public CompareException(ErrorCode errorCode) {
        super(errorCode);
    }
}
