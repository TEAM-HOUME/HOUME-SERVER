package or.sopt.houme.global.api.handler;

import lombok.Getter;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoListResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

// Fallback 이미지 전용 Exception
@Getter
public class ImageFallbackException extends GeneralException {

  private final ImageInfoListResponse imageInfoListResponse;

    public ImageFallbackException(ErrorCode errorCode, ImageInfoListResponse imageInfoListResponse) {
        super(errorCode);
        this.imageInfoListResponse = imageInfoListResponse;   // 사용자 반환을 위한 ImageInfoListResponse
    }
}
