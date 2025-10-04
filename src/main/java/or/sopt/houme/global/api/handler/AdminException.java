package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class AdminException extends GeneralException {
    public AdminException(ErrorCode errorCode) {
        super(errorCode);
    }
}
