package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class CreditException extends GeneralException {
    public CreditException(ErrorCode errorCode) {
        super(errorCode);
    }
}
