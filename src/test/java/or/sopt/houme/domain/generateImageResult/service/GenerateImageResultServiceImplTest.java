package or.sopt.houme.domain.generateImageResult.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.repository.GenerateImageUsedProductRepository;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.handler.ValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateImageResultServiceImplTest {

    @InjectMocks
    private GenerateImageResultServiceImpl generateImageResultService;

    @Mock
    private GenerateImageService generateImageService;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private GenerateImageUsedProductRepository generateImageUsedProductRepository;

    @Mock
    private HouseService houseService;

    @Test
    @DisplayName("LIST 타입이 아닌 이미지 조회 요청이면 예외가 발생한다")
    void getListResultItems_throws_whenGenerationTypeIsNotList() {
        GenerateImage recommendImage = GenerateImage.builder()
                .id(1L)
                .url("https://image")
                .generationType(GenerateImageType.RECOMMEND)
                .build();

        when(generateImageService.findGenerateImage(1L)).thenReturn(recommendImage);

        User user = mock(User.class);

        assertThatThrownBy(() -> generateImageResultService.getListResultItems(user, 1L))
                .isInstanceOf(ValidException.class);
    }

    @Test
    @DisplayName("LIST 타입 이미지 조회 시 배너에 매핑된 raw product 목록을 응답한다")
    void getListResultItems_returnsProductsFromBanner() {
        Banner bannerRef = Banner.builder()
                .id(10L)
                .build();

        GenerateImage listImage = GenerateImage.builder()
                .id(1L)
                .url("https://generated-image")
                .generationType(GenerateImageType.LIST)
                .banner(bannerRef)
                .build();

        CurationRawProduct rawProduct = CurationRawProduct.builder()
                .id(101L)
                .productName("테스트 상품")
                .productImageUrl("https://product-image")
                .listPrice(39900L)
                .discountRate(30)
                .discountPrice(27990L)
                .productSiteUrl("https://product-link")
                .build();

        BannerCurationRawProduct mapping = BannerCurationRawProduct.builder()
                .id(1L)
                .curationRawProduct(rawProduct)
                .build();

        Banner bannerWithRawProducts = Banner.builder()
                .id(10L)
                .bannerRawProducts(List.of(mapping))
                .build();

        when(generateImageService.findGenerateImage(1L)).thenReturn(listImage);
        when(bannerRepository.findAllByIdInWithRawProducts(List.of(10L))).thenReturn(List.of(bannerWithRawProducts));

        User user = mock(User.class);

        GenerateImageResultResponse response = generateImageResultService.getListResultItems(user, 1L);

        assertThat(response.imageId()).isEqualTo(1L);
        assertThat(response.imageUrl()).isEqualTo("https://generated-image");
        assertThat(response.isMirror()).isFalse();
        assertThat(response.products()).hasSize(1);
        assertThat(response.products().getFirst().id()).isEqualTo(101L);
        assertThat(response.products().getFirst().name()).isEqualTo("테스트 상품");
    }
}
