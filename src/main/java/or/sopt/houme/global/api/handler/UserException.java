package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class UserException extends GeneralException {
    /**
     * 지정된 에러 코드를 사용하여 UserException 인스턴스를 생성합니다.
     *
     * @param errorCode 예외에 해당하는 에러 코드
     */
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
