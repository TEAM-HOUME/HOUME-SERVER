package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class TokenException extends GeneralException {
    /**
     * 토큰 관련 오류가 발생했을 때 지정된 에러 코드를 사용하여 TokenException을 생성합니다.
     *
     * @param errorCode 발생한 토큰 오류에 해당하는 에러 코드
     */
    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
