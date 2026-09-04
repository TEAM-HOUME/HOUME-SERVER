package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class PriceCompareException extends GeneralException {
    public PriceCompareException(ErrorCode errorCode) {
        super(errorCode);
    }
}
