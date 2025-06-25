package or.sopt.houme.global.api;

public class UserException extends GeneralException{
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
