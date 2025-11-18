package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class PreferenceException extends GeneralException {
    public PreferenceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
