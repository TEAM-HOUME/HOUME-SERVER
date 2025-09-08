package or.sopt.houme.global.api.handler;

import lombok.Getter;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

// Fallback 이미지 전용 Exception
@Getter
public class ImageFallbackException extends GeneralException {

  private final Object imageInfo;

    public ImageFallbackException(ErrorCode errorCode, Object imageInfo) {
        super(errorCode);
        this.imageInfo = imageInfo;   // 사용자 반환을 위한 Object (ImageInfoListResponse와 ImageInfoResponse 둘 다 처리하기 위함)
    }
}
