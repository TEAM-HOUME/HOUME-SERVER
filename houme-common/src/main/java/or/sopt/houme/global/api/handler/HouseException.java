package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class HouseException extends GeneralException {
  public HouseException(ErrorCode errorCode) {
    super(errorCode);
  }
}
