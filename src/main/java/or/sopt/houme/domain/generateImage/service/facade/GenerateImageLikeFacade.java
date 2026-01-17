package or.sopt.houme.domain.generateImage.service.facade;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.house.presentation.dto.request.IsLikeRequest;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.preference.service.FactorService;
import or.sopt.houme.domain.preference.service.GenerateImagePreferenceService;
import or.sopt.houme.domain.preference.service.PreferenceService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.GenerateImageException;
import or.sopt.houme.global.api.handler.PreferenceException;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static or.sopt.houme.global.util.constant.OptimisticLockConstant.MAX_RETRIES;
import static or.sopt.houme.global.util.constant.OptimisticLockConstant.RETRY_DELAY_MS;

@Component
@RequiredArgsConstructor
public class GenerateImageLikeFacade {

    private final GenerateImageService generateImageService;
    private final GenerateImagePreferenceService generateImagePreferenceService;
    private final FactorService factorService;
    private final PreferenceService preferenceService;

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
                generateImagePreferenceService.toggleGenerateImagePreference(generateImage, request.isLike());
                // 성공시 종료
                return;
            } catch (OptimisticLockException | DataIntegrityViolationException e){
                // unique 제약 조건 위반 시 재시도
                // 지수 백오프 => 서버 부하 감소, 성공률 증가
                long backoffTime = (long) Math.pow(2, retryCount) * RETRY_DELAY_MS;
                Thread.sleep(backoffTime);
                retryCount++;
            }
        }

        // 재시도 횟수 초과 시 예외 처리
        throw new GenerateImageException(ErrorCode.GENERATE_IMAGE_RETRY_EXCEPTION);
    }

    // 생성된 이미지 선호도 삭제
    @Transactional
    public void deletePreference(User user, Long generatedImageId){
        // 생성된 이미지 조회
        GenerateImage generateImage = generateImageService.findGenerateImage(generatedImageId);
        if (!Objects.equals(generateImage.getHouse().getUser().getId(), user.getId())) {
            throw new UserException(ErrorCode.USER_ROLE_EXCEPTION);
        }

        // GenerateImagePreference 삭제
        Long preferenceId = generateImagePreferenceService.deleteGenerateImagePreference(generateImage);

        // 해당 Preference가 없다면 예외처리
        if (preferenceId == null) {
            throw new PreferenceException(ErrorCode.NOT_FOUND_PREFERENCE);
        }

        // PreferenceFactor 삭제
        factorService.deletePreferenceFactor(preferenceId);

        // Preference 삭제
        preferenceService.deletePreference(preferenceId);
    }

}
