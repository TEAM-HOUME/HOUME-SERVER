package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class TokenException extends GeneralException {
    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
