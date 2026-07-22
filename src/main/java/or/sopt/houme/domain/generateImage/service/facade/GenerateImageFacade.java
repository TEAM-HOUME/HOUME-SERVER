package or.sopt.houme.domain.generateImage.service.facade;

import or.sopt.houme.domain.generateImage.presentation.dto.request.BannerGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageV4Request;
import or.sopt.houme.domain.generateImage.presentation.dto.request.OtherStyleGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.ProductGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.response.BannerGenerateImageResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.GenerateImageV4Response;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoListResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.OtherStyleGenerateImageResponse;
import or.sopt.houme.user.domain.User;

/**
 * 이미지 생성 파사드 인바운드 계약 (#582 — 구현은 엔티티 그래프를 다루므로 infra 측에 배정).
 */
public interface GenerateImageFacade {

    ImageInfoResponse generateImage(User user, GenerateImageRequest generateImageRequest);

    ImageInfoResponse generateImageByGemini(User user, GenerateImageRequest generateImageRequest);

    ImageInfoResponse generateImageByFastApi(User user, GenerateImageRequest generateImageRequest);

    ImageInfoResponse generateImageByFastApiGemini(User user, GenerateImageRequest generateImageRequest);

    ImageInfoListResponse generateImageBy2ea(User user, GenerateImageRequest generateImageRequest);

    ImageInfoListResponse generateImageBy2eaGemini(User user, GenerateImageRequest generateImageRequest);

    BannerGenerateImageResponse generateBannerImageByGemini(User user, BannerGenerateImageRequest request);

    OtherStyleGenerateImageResponse generateOtherStyleImageByGemini(User user, OtherStyleGenerateImageRequest request);

    GenerateImageV4Response generateImageV4ByGemini(User user, GenerateImageV4Request request);

    GenerateImageV4Response generateImageByProducts(User user, ProductGenerateImageRequest request);

    ImageInfoResponse getFallBackImage(User user, Long houseId);
}
