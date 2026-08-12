package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class ChatGptException extends GeneralException {
    public ChatGptException(ErrorCode errorCode) {
        super(errorCode);
    }
}
