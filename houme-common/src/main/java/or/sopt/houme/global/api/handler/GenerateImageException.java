package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class GenerateImageException extends GeneralException {
    public GenerateImageException(ErrorCode errorCode) {
        super(errorCode);
    }
}
