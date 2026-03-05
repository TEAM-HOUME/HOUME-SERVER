package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class FurnitureException extends GeneralException {
    public FurnitureException(ErrorCode errorCode) {
        super(errorCode);
    }
}
