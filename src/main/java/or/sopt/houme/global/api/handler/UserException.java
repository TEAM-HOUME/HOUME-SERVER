package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class UserException extends GeneralException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
