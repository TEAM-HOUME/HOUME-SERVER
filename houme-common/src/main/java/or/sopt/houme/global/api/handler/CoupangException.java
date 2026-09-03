package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class CoupangException extends GeneralException {

    public CoupangException(ErrorCode errorCode) {
        super(errorCode);
    }
}
