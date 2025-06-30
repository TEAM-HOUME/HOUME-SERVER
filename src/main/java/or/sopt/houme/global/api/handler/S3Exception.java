package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class S3Exception extends GeneralException {
    public S3Exception(ErrorCode errorCode) {
        super(errorCode);
    }
}
