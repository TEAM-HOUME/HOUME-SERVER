package or.sopt.houme.domain.house;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.house.dto.request.IsLikeRequest;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.preference.service.PromptPreferenceService;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.GenerateImageException;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import static or.sopt.houme.global.util.constant.OptimisticLockConstant.MAX_RETRIES;
import static or.sopt.houme.global.util.constant.OptimisticLockConstant.RETRY_DELAY_MS;

@Component
@RequiredArgsConstructor
public class HouseLikeFacade {

    private final GenerateImageService generateImageService;
    private final PromptPreferenceService promptPreferenceService;

    // 생성된 이미지 선호도
    public void isLike(User user, Long generatedImageId, IsLikeRequest request) throws InterruptedException {
        int retryCount = 0;

        // 도면 이미지 조회
        GenerateImage generateImage = generateImageService.findGenerateImage(generatedImageId);

        House house = generateImage.getHouse();

        // 본인이 생성한 이미지가 아니라면 에러 처리
        if (!house.getUser().getId().equals(user.getId())) {
            throw new UserException(ErrorCode.USER_ROLE_EXCEPTION);
        }

        while (retryCount < MAX_RETRIES){
            try {
                promptPreferenceService.togglePromptPreference(house, request.isLike());
                // 성공시 종료
                return;
            } catch (DataIntegrityViolationException e){
                // unique 제약 조건 위반 시 재시도
                long backoffTime = (long) Math.pow(2, retryCount) * RETRY_DELAY_MS;
                Thread.sleep(backoffTime);
                retryCount++;
            }
        }

        // 재시도 횟수 초과 시 예외 처리
        throw new GenerateImageException(ErrorCode.PROMPT_RETRY_EXCEPTION);
    }

}
