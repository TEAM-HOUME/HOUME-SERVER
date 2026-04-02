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
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.repository.CurationRawProductFurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.generateImage.repository.GenerateImageUsedProductRepository;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.RelatedImagesResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.SimilarItemsResponse;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.handler.ValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateImageResultServiceImplTest {

    @InjectMocks
    private GenerateImageResultServiceImpl generateImageResultService;

    @Mock
    private GenerateImageService generateImageService;

    @Mock
    private GenerateImageRepository generateImageRepository;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private GenerateImageUsedProductRepository generateImageUsedProductRepository;

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Mock
    private CurationRawProductFurnitureTagRepository curationRawProductFurnitureTagRepository;

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

    @Test
    @DisplayName("유사 상품 조회는 가구타입 우선 후보를 먼저 채우고 최대 4개를 반환한다")
    void getSimilarItems_prioritizesFurnitureTypeAndLimitsToFour() {
        Banner bannerRef = Banner.builder().id(10L).build();
        GenerateImage listImage = GenerateImage.builder()
                .id(1L)
                .generationType(GenerateImageType.LIST)
                .banner(bannerRef)
                .build();

        CurationRawProduct selected = CurationRawProduct.builder()
                .id(101L)
                .brand("선택브랜드")
                .productName("선택 상품")
                .build();

        BannerCurationRawProduct mapping = BannerCurationRawProduct.builder()
                .id(1L)
                .curationRawProduct(selected)
                .build();
        Banner bannerWithRawProducts = Banner.builder()
                .id(10L)
                .bannerRawProducts(List.of(mapping))
                .build();

        FurnitureType furnitureType = FurnitureType.builder().id(1L).build();
        Furniture furniture = Furniture.builder().id(1L).furnitureType(furnitureType).build();
        Tag tag = Tag.builder().id(1L).build();
        FurnitureTag furnitureTag = FurnitureTag.builder().id(1L).furniture(furniture).tag(tag).build();
        CurationRawProductFurnitureTag selectedProductMapping = CurationRawProductFurnitureTag.builder()
                .id(1L)
                .furnitureTag(furnitureTag)
                .build();

        CurationRawProduct c1 = CurationRawProduct.builder().id(201L).productName("furniture-type-1").build();
        CurationRawProduct c2 = CurationRawProduct.builder().id(202L).productName("furniture-type-2").build();
        CurationRawProduct c3 = CurationRawProduct.builder().id(203L).productName("furniture-type-3").build();
        CurationRawProduct c4 = CurationRawProduct.builder().id(204L).productName("furniture-type-4").build();

        when(generateImageService.findGenerateImage(1L)).thenReturn(listImage);
        when(bannerRepository.findAllByIdInWithRawProducts(List.of(10L))).thenReturn(List.of(bannerWithRawProducts));
        when(curationRawProductFurnitureTagRepository.findAllByCurationRawProductIdInWithFurnitureTag(List.of(101L)))
                .thenReturn(List.of(selectedProductMapping));
        when(curationRawProductRepository.findAllSimilarByFurnitureTypeIds(eq(List.of(1L)), eq(List.of(101L)), any()))
                .thenReturn(List.of(c1, c2, c3, c4));

        User user = mock(User.class);
        SimilarItemsResponse response = generateImageResultService.getSimilarItems(user, 1L);

        assertThat(response.products()).hasSize(4);
        assertThat(response.products().get(0).id()).isEqualTo(201L);
        assertThat(response.products().get(3).id()).isEqualTo(204L);
    }

    @Test
    @DisplayName("related-images는 LIST 타입 기준으로 현재 이미지를 제외하고 최신순/중복제거 결과를 반환한다")
    void getRelatedImages_returnsLatestDistinctImagesExcludingCurrent() {
        Banner bannerRef = Banner.builder().id(10L).build();
        GenerateImage current = GenerateImage.builder()
                .id(1L)
                .generationType(GenerateImageType.LIST)
                .banner(bannerRef)
                .build();

        CurationRawProduct selected = CurationRawProduct.builder()
                .id(101L)
                .productName("선택 상품")
                .build();
        BannerCurationRawProduct mapping = BannerCurationRawProduct.builder()
                .id(1L)
                .curationRawProduct(selected)
                .build();
        Banner bannerWithRawProducts = Banner.builder()
                .id(10L)
                .bannerRawProducts(List.of(mapping))
                .build();

        GenerateImage related1 = GenerateImage.builder()
                .id(200L)
                .url("https://image-200")
                .generationType(GenerateImageType.LIST)
                .build();
        GenerateImage related2 = GenerateImage.builder()
                .id(199L)
                .url("https://image-199")
                .generationType(GenerateImageType.RECOMMEND)
                .build();

        when(generateImageService.findGenerateImage(1L)).thenReturn(current);
        when(bannerRepository.findAllByIdInWithRawProducts(List.of(10L))).thenReturn(List.of(bannerWithRawProducts));
        when(generateImageRepository.findRelatedImagesByRawProductIds(List.of(101L), 1L, 10))
                .thenReturn(List.of(related1, related2));

        User user = mock(User.class);
        when(user.getDisplayName()).thenReturn("최윤하");

        RelatedImagesResponse response = generateImageResultService.getRelatedImages(user, 1L);

        assertThat(response.name()).isEqualTo("최윤하");
        assertThat(response.images()).hasSize(1);
        assertThat(response.images().get(0).id()).isEqualTo(200L);
        assertThat(response.images().get(0).resultType()).isEqualTo("LIST");
    }
}
