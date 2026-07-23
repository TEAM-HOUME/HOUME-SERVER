package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class TagException extends GeneralException {
    public TagException(ErrorCode errorCode) {
        super(errorCode);
    }
}
