package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class CarouselException extends GeneralException {
    public CarouselException(ErrorCode errorCode) {
        super(errorCode);
    }
}
