package or.sopt.houme.domain.house;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.house.dto.request.IsLikeRequest;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.preference.entity.Preference;
import or.sopt.houme.domain.preference.service.PreferenceService;
import or.sopt.houme.domain.preference.service.PromptPreferenceService;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HouseLikeFacade {

    private final GenerateImageService generateImageService;
    private final PreferenceService preferenceService;
    private final PromptPreferenceService promptPreferenceService;

    // 생성된 이미지 선호도
    public void isLike(User user, Long generatedImageId, IsLikeRequest request) {

        // 도면 이미지 조회
        GenerateImage generateImage = generateImageService.findGenerateImage(generatedImageId);

        House house = generateImage.getHouse();

        // 본인이 생성한 이미지가 아니라면 에러 처리
        if (!house.getUser().getId().equals(user.getId())) {
            throw new UserException(ErrorCode.USER_ROLE_EXCEPTION);
        }
        // 좋아요 생성
        Preference preference = preferenceService.createPreference(request.isLike());

        // 집 프롬프트 좋아요 생성
        promptPreferenceService.createPromptPreference(house, preference);
    }
}
