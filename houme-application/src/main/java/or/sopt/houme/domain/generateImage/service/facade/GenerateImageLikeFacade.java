package or.sopt.houme.domain.generateImage.service.facade;

import or.sopt.houme.domain.house.presentation.dto.request.IsLikeRequest;
import or.sopt.houme.user.domain.User;

/** 생성이미지 좋아요 파사드 인바운드 계약 (#582). */
public interface GenerateImageLikeFacade {

    void isLike(User user, Long generatedImageId, IsLikeRequest request) throws InterruptedException;

    void deletePreference(User user, Long generatedImageId);
}
