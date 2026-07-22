package or.sopt.houme.global.api.handler;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public class NaverApiException extends GeneralException {
  public NaverApiException(ErrorCode errorCode) {
    super(errorCode);
  }
}
