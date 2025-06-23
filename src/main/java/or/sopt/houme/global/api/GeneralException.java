package or.sopt.houme.global.api;

import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {  // 예외클래스 상속의 중간층
    private final ErrorCode errorCode;

    public GeneralException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
    }
}
