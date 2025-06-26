package or.sopt.houme.global.api;

public class TokenException extends GeneralException{
    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
