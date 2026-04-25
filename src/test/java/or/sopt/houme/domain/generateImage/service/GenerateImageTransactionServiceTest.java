package or.sopt.houme.domain.generateImage.service;

import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.repository.GenerateImageUsedProductRepository;
import or.sopt.houme.domain.generateImage.presentation.dto.response.BannerGenerateImageResponse;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[GenerateImageTransaction Service] Test")
class GenerateImageTransactionServiceTest {

    @InjectMocks
    private GenerateImageTransactionService generateImageTransactionService;

    @Mock
    private CreditService creditService;

    @Mock
    private HouseService houseService;

    @Mock
    private GenerateImageService generateImageService;

    @Mock
    private GenerateImageUsedProductRepository generateImageUsedProductRepository;

    @Mock
    private UserService userService;

    @Test
    @DisplayName("배너 이미지 저장 성공 시 House 생성, 이미지 저장, 크레딧 확정, 사용자 상태 업데이트를 수행한다")
    void saveBannerImageAndConfirmCredit_success() {
        User user = mock(User.class);
        Credit lockedCredit = mock(Credit.class);
        Banner banner = mock(Banner.class);
        House house = mock(House.class);
        BannerCurationRawProduct bannerMapping = mock(BannerCurationRawProduct.class);
        CurationRawProduct rawProduct = mock(CurationRawProduct.class);

        Long floorPlanId = 3L;
        boolean isMirror = true;
        String finalPrompt = "final prompt";
        ImageUploadResponseDTO imageResponse = ImageUploadResponseDTO.from(
                "file.jpg",
                "origin.jpg",
                "https://cdn.example.com/file.jpg",
                "image/jpeg"
        );

        GenerateImage generateImage = GenerateImage.builder()
                .id(101L)
                .url("https://cdn.example.com/file.jpg")
                .filename("file.jpg")
                .originalFilename("origin.jpg")
                .fileExtension("image/jpeg")
                .house(house)
                .generationType(GenerateImageType.LIST)
                .build();

        when(houseService.createTemplateHouse(user, banner, finalPrompt, floorPlanId, isMirror))
                .thenReturn(house);
        when(generateImageService.createGenerateImage(imageResponse, house, GenerateImageType.LIST))
                .thenReturn(generateImage);
        when(rawProduct.getId()).thenReturn(1L);
        when(bannerMapping.getCurationRawProduct()).thenReturn(rawProduct);
        when(banner.getBannerRawProducts()).thenReturn(List.of(bannerMapping));

        BannerGenerateImageResponse response = generateImageTransactionService.saveBannerImageAndConfirmCredit(
                user,
                lockedCredit,
                banner,
                floorPlanId,
                isMirror,
                finalPrompt,
                imageResponse
        );

        assertThat(response.imageId()).isEqualTo(101L);
        verify(houseService).createTemplateHouse(user, banner, finalPrompt, floorPlanId, isMirror);
        verify(generateImageService).createGenerateImage(imageResponse, house, GenerateImageType.LIST);
        verify(generateImageUsedProductRepository).saveAll(anyList());
        verify(creditService).commitCreditDeletion(lockedCredit);
        verify(userService).updateHasGeneratedImage(user);
    }

    @Test
    @DisplayName("이미지 저장 단계에서 예외가 발생하면 크레딧 확정과 사용자 상태 업데이트를 수행하지 않는다")
    void saveBannerImageAndConfirmCredit_failBeforeCreditCommit() {
        User user = mock(User.class);
        Credit lockedCredit = mock(Credit.class);
        Banner banner = mock(Banner.class);
        House house = mock(House.class);
        ImageUploadResponseDTO imageResponse = ImageUploadResponseDTO.from(
                "file.jpg",
                "origin.jpg",
                "https://cdn.example.com/file.jpg",
                "image/jpeg"
        );

        when(houseService.createTemplateHouse(user, banner, "prompt", 1L, false)).thenReturn(house);
        when(generateImageService.createGenerateImage(imageResponse, house, GenerateImageType.LIST))
                .thenThrow(new RuntimeException("image save failed"));

        assertThatThrownBy(() -> generateImageTransactionService.saveBannerImageAndConfirmCredit(
                user, lockedCredit, banner, 1L, false, "prompt", imageResponse
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("image save failed");

        verify(generateImageUsedProductRepository, never()).saveAll(anyList());
        verify(creditService, never()).commitCreditDeletion(any());
        verify(userService, never()).updateHasGeneratedImage(any());
    }

    @Test
    @DisplayName("크레딧 확정 단계에서 예외가 발생하면 사용자 상태 업데이트를 수행하지 않는다")
    void saveBannerImageAndConfirmCredit_failOnCreditCommit() {
        User user = mock(User.class);
        Credit lockedCredit = mock(Credit.class);
        Banner banner = mock(Banner.class);
        House house = mock(House.class);
        BannerCurationRawProduct bannerMapping = mock(BannerCurationRawProduct.class);
        CurationRawProduct rawProduct = mock(CurationRawProduct.class);
        ImageUploadResponseDTO imageResponse = ImageUploadResponseDTO.from(
                "file.jpg",
                "origin.jpg",
                "https://cdn.example.com/file.jpg",
                "image/jpeg"
        );

        GenerateImage generateImage = GenerateImage.builder()
                .id(102L)
                .url("https://cdn.example.com/file.jpg")
                .filename("file.jpg")
                .originalFilename("origin.jpg")
                .fileExtension("image/jpeg")
                .house(house)
                .generationType(GenerateImageType.LIST)
                .build();

        when(houseService.createTemplateHouse(user, banner, "prompt", 2L, true)).thenReturn(house);
        when(generateImageService.createGenerateImage(imageResponse, house, GenerateImageType.LIST))
                .thenReturn(generateImage);
        when(rawProduct.getId()).thenReturn(1L);
        when(bannerMapping.getCurationRawProduct()).thenReturn(rawProduct);
        when(banner.getBannerRawProducts()).thenReturn(List.of(bannerMapping));
        doThrow(new RuntimeException("credit commit failed"))
                .when(creditService).commitCreditDeletion(lockedCredit);

        assertThatThrownBy(() -> generateImageTransactionService.saveBannerImageAndConfirmCredit(
                user, lockedCredit, banner, 2L, true, "prompt", imageResponse
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("credit commit failed");

        verify(generateImageUsedProductRepository).saveAll(anyList());
        verify(creditService).commitCreditDeletion(lockedCredit);
        verify(userService, never()).updateHasGeneratedImage(any());
    }
}
