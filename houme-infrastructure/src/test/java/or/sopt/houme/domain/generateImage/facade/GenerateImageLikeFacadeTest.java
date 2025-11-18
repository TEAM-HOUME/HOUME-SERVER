package or.sopt.houme.domain.generateImage.facade;

import jakarta.persistence.OptimisticLockException;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.house.dto.request.IsLikeRequest;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.preference.service.FactorService;
import or.sopt.houme.domain.preference.service.GenerateImagePreferenceService;
import or.sopt.houme.domain.preference.service.PreferenceService;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.GenerateImageException;
import or.sopt.houme.global.api.handler.PreferenceException;
import or.sopt.houme.global.api.handler.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateImageLikeFacadeTest {

    @InjectMocks
    private GenerateImageLikeFacade generateImageLikeFacade;

    @Mock
    private GenerateImageService generateImageService;

    @Mock
    private GenerateImagePreferenceService generateImagePreferenceService;

    @Mock
    private FactorService factorService;

    @Mock
    private PreferenceService preferenceService;

    private User testUser;
    private User otherUser;
    private House testHouse;
    private GenerateImage testGenerateImage;
    private IsLikeRequest likeRequest;
    private IsLikeRequest unlikeRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).build();
        otherUser = User.builder().id(2L).build();

        testHouse = House.builder().id(10L).user(testUser).build();
        testGenerateImage = GenerateImage.builder().id(100L).house(testHouse).build();
        likeRequest = new IsLikeRequest(true);
        unlikeRequest = new IsLikeRequest(false);
    }

    /**
     * === isLike 메서드 테스트 ===
     */
    @DisplayName("isLike - 성공: 이미지 소유자가 좋아요 토글")
    @Test
    void isLike_success() throws InterruptedException {
        // Given
        when(generateImageService.findGenerateImage(anyLong())).thenReturn(testGenerateImage);
        doNothing().when(generateImagePreferenceService).toggleGenerateImagePreference(any(GenerateImage.class), anyBoolean());

        // When
        generateImageLikeFacade.isLike(testUser, testGenerateImage.getId(), likeRequest);

        // Then
        verify(generateImageService, times(1)).findGenerateImage(testGenerateImage.getId());
        verify(generateImagePreferenceService, times(1)).toggleGenerateImagePreference(testGenerateImage, likeRequest.isLike());
    }

    @Test
    @DisplayName("isLike - 실패: 이미지를 찾을 수 없음 (하위 서비스 에러)")
    void isLike_notFoundGenerateImage() {
        // Given
        when(generateImageService.findGenerateImage(anyLong()))
                .thenThrow(new GenerateImageException(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY));

        // When & Then
        assertThatThrownBy(() -> generateImageLikeFacade.isLike(testUser, testGenerateImage.getId(), likeRequest))
                .isInstanceOf(GenerateImageException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY.getMsg());
        verify(generateImageService, times(1)).findGenerateImage(testGenerateImage.getId());
        verify(generateImagePreferenceService, never()).toggleGenerateImagePreference(any(), anyBoolean());
    }

    @Test
    @DisplayName("isLike - 실패: 이미지 소유자가 아님")
    void isLike_userNotOwner() {
        // Given
        when(generateImageService.findGenerateImage(anyLong())).thenReturn(testGenerateImage);

        // When & Then
        assertThatThrownBy(() -> generateImageLikeFacade.isLike(otherUser, testGenerateImage.getId(), likeRequest))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(ErrorCode.USER_ROLE_EXCEPTION.getMsg());
        verify(generateImageService, times(1)).findGenerateImage(testGenerateImage.getId());
        verify(generateImagePreferenceService, never()).toggleGenerateImagePreference(any(), anyBoolean());
    }

    @Test
    @DisplayName("isLike - 성공: OptimisticLockException 발생 후 재시도 성공")
    void isLike_retrySuccessAfterOptimisticLock() throws InterruptedException {
        // Given
        when(generateImageService.findGenerateImage(anyLong())).thenReturn(testGenerateImage);
        // 첫 호출은 OptimisticLockException 발생, 두 번째 호출은 성공
        doThrow(new OptimisticLockException())
                .doNothing() // 두 번째 호출 시 성공
                .when(generateImagePreferenceService).toggleGenerateImagePreference(any(GenerateImage.class), anyBoolean());

        // When
        generateImageLikeFacade.isLike(testUser, testGenerateImage.getId(), likeRequest);

        // Then
        verify(generateImagePreferenceService, times(2)).toggleGenerateImagePreference(testGenerateImage, likeRequest.isLike());
    }

    @Test
    @DisplayName("isLike - 실패: OptimisticLockException 재시도 횟수 초과")
    void isLike_retryExceedsMaxAttempts() {
        // Given
        when(generateImageService.findGenerateImage(anyLong())).thenReturn(testGenerateImage);
        // 모든 호출에서 OptimisticLockException 발생 (MAX_RETRIES + 1 번 호출)
        doThrow(new OptimisticLockException())
                .when(generateImagePreferenceService).toggleGenerateImagePreference(any(GenerateImage.class), anyBoolean());

        // When & Then
        assertThatThrownBy(() -> generateImageLikeFacade.isLike(testUser, testGenerateImage.getId(), likeRequest))
                .isInstanceOf(GenerateImageException.class)
                .hasMessageContaining(ErrorCode.GENERATE_IMAGE_RETRY_EXCEPTION.getMsg());
        verify(generateImagePreferenceService, atLeast(3)).toggleGenerateImagePreference(testGenerateImage, likeRequest.isLike()); // MAX_RETRIES가 3이라면
    }

    @Test
    @DisplayName("isLike - 성공: DataIntegrityViolationException 발생 후 재시도 성공")
    void isLike_retrySuccessAfterDataIntegrityViolation() throws InterruptedException {
        // Given
        when(generateImageService.findGenerateImage(anyLong())).thenReturn(testGenerateImage);
        // 첫 호출은 DataIntegrityViolationException 발생, 두 번째 호출은 성공
        doThrow(new DataIntegrityViolationException("Unique constraint violation"))
                .doNothing() // 두 번째 호출 시 성공
                .when(generateImagePreferenceService).toggleGenerateImagePreference(any(GenerateImage.class), anyBoolean());

        // When
        generateImageLikeFacade.isLike(testUser, testGenerateImage.getId(), likeRequest);

        // Then
        verify(generateImagePreferenceService, times(2)).toggleGenerateImagePreference(testGenerateImage, likeRequest.isLike());
    }

    /**
     * === deletePreference 메서드 테스트 ===
     */
    @Test
    @DisplayName("deletePreference - 성공: 이미지 선호도 및 관련 데이터 삭제")
    void deletePreference_success() {
        // Given
        Long preferenceId = 200L;
        when(generateImageService.findGenerateImage(anyLong())).thenReturn(testGenerateImage);
        when(generateImagePreferenceService.deleteGenerateImagePreference(any(GenerateImage.class))).thenReturn(preferenceId);
        doNothing().when(factorService).deletePreferenceFactor(anyLong());
        doNothing().when(preferenceService).deletePreference(anyLong());

        // When
        generateImageLikeFacade.deletePreference(testUser, testGenerateImage.getId());

        // Then
        verify(generateImageService, times(1)).findGenerateImage(testGenerateImage.getId());
        verify(generateImagePreferenceService, times(1)).deleteGenerateImagePreference(testGenerateImage);
        verify(factorService, times(1)).deletePreferenceFactor(preferenceId);
        verify(preferenceService, times(1)).deletePreference(preferenceId);
    }

    @Test
    @DisplayName("deletePreference - 실패: 이미지를 찾을 수 없음 (하위 서비스 에러)")
    void deletePreference_notFoundGenerateImage() {
        // Given
        when(generateImageService.findGenerateImage(anyLong()))
                .thenThrow(new GenerateImageException(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY));

        // When & Then
        assertThatThrownBy(() -> generateImageLikeFacade.deletePreference(testUser, testGenerateImage.getId()))
                .isInstanceOf(GenerateImageException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY.getMsg());
        verify(generateImageService, times(1)).findGenerateImage(testGenerateImage.getId());
        verifyNoInteractions(generateImagePreferenceService, factorService, preferenceService); // 다른 서비스와 상호작용 없는지 확인
    }

    @Test
    @DisplayName("deletePreference - 실패: 이미지 소유자가 아님")
    void deletePreference_userNotOwner() {
        // Given
        when(generateImageService.findGenerateImage(anyLong())).thenReturn(testGenerateImage);

        // When & Then
        assertThatThrownBy(() -> generateImageLikeFacade.deletePreference(otherUser, testGenerateImage.getId()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(ErrorCode.USER_ROLE_EXCEPTION.getMsg());
        verify(generateImageService, times(1)).findGenerateImage(testGenerateImage.getId());
        verifyNoInteractions(generateImagePreferenceService, factorService, preferenceService);
    }

    @Test
    @DisplayName("deletePreference - 실패: Preference를 찾을 수 없음 (deleteGenerateImagePreference 결과 null)")
    void deletePreference_notFoundPreferenceAfterDeleteGIP() {
        // Given
        when(generateImageService.findGenerateImage(anyLong())).thenReturn(testGenerateImage);
        when(generateImagePreferenceService.deleteGenerateImagePreference(any(GenerateImage.class))).thenReturn(null); // preferenceId가 null 반환

        // When & Then
        assertThatThrownBy(() -> generateImageLikeFacade.deletePreference(testUser, testGenerateImage.getId()))
                .isInstanceOf(PreferenceException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_PREFERENCE.getMsg());
        verify(generateImageService, times(1)).findGenerateImage(testGenerateImage.getId());
        verify(generateImagePreferenceService, times(1)).deleteGenerateImagePreference(testGenerateImage);
        verifyNoInteractions(factorService, preferenceService); // Factor와 Preference 서비스는 호출되지 않아야 함
    }
}