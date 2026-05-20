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
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductFurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRawProductRepository;
import or.sopt.houme.domain.generateImage.repository.GenerateImageUsedProductRepository;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GeneratedImageMetaResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.RelatedImagesResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.SimilarItemsResponse;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.handler.GenerateImageException;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private GenerateImageRawProductRepository generateImageRawProductRepository;

    @Mock
    private CurationRawProductColorRepository curationRawProductColorRepository;

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Mock
    private CurationRawProductFurnitureTagRepository curationRawProductFurnitureTagRepository;

    @Mock
    private RecommendFurnitureRepository recommendFurnitureRepository;

    @Mock
    private JjymRepository jjymRepository;

    @Mock
    private HouseService houseService;

    @Test
    @DisplayName("이미지 메타 조회 시 imageUrl과 isMirror를 반환한다")
    void getGeneratedImageMeta_returnsImageUrlAndIsMirror() {
        House house = House.builder().id(100L).build();
        GenerateImage image = GenerateImage.builder()
                .id(1L)
                .url("https://generated-image")
                .house(house)
                .build();
        User user = User.builder().id(1L).build();

        when(generateImageService.findGenerateImage(1L)).thenReturn(image);
        when(houseService.getIsMirrorByHouseId(100L)).thenReturn(true);

        GeneratedImageMetaResponse response = generateImageResultService.getGeneratedImageMeta(user, 1L);

        assertThat(response.imageId()).isEqualTo(1L);
        assertThat(response.imageUrl()).isEqualTo("https://generated-image");
        assertThat(response.isMirror()).isTrue();
    }

    @Test
    @DisplayName("목록형 지원 타입(BANNER/STYLE/PRODUCT)이 아닌 이미지 조회 요청이면 예외가 발생한다")
    void getListResultItems_throws_whenGenerationTypeIsNotListFamily() {
        GenerateImage recommendImage = GenerateImage.builder()
                .id(1L)
                .url("https://image")
                .generationType(GenerateImageType.FULL_FUNNEL)
                .build();

        when(generateImageService.findGenerateImage(1L)).thenReturn(recommendImage);

        User user = mock(User.class);

        assertThatThrownBy(() -> generateImageResultService.getListResultItems(user, 1L))
                .isInstanceOf(GenerateImageException.class);
    }

    @Test
    @DisplayName("BANNER 타입 이미지 조회 시 배너에 매핑된 raw product 목록을 응답한다")
    void getListResultItems_returnsProductsFromBanner() {
        Banner bannerRef = Banner.builder()
                .id(10L)
                .build();
      
        House house = House.builder()
                .id(100L)
                .banner(bannerRef)
                .build();

        GenerateImage listImage = GenerateImage.builder()
                .id(1L)
                .url("https://generated-image")
                .generationType(GenerateImageType.BANNER)
                .house(house)
                .build();

        CurationRawProduct rawProduct = CurationRawProduct.builder()
                .id(101L)
                .productId(1001L)
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
        when(curationRawProductColorRepository.findAllByCurationRawProductIdIn(List.of(101L)))
                .thenReturn(List.of(
                        CurationRawProductColor.builder()
                                .id(1L)
                                .curationRawProduct(rawProduct)
                                .clientColorName("화이트")
                                .build(),
                        CurationRawProductColor.builder()
                                .id(2L)
                                .curationRawProduct(rawProduct)
                                .rawColorName("아이보리")
                                .build()
                ));
        when(recommendFurnitureRepository.findAllBySourceAndFurnitureProductIdIn(CurationSource.RAW, List.of(1001L)))
                .thenReturn(List.of(RecommendFurniture.builder()
                        .id(501L)
                        .source(CurationSource.RAW)
                        .furnitureProductId(1001L)
                        .build()));
        when(jjymRepository.findAllByUserIdAndRecommendFurnitureIdIn(1L, List.of(501L))).thenReturn(List.of());

        User user = User.builder().id(1L).build();

        GenerateImageResultResponse response = generateImageResultService.getListResultItems(user, 1L);

        assertThat(response.imageId()).isEqualTo(1L);
        assertThat(response.products()).hasSize(1);
        assertThat(response.products().getFirst().id()).isEqualTo(101L);
        assertThat(response.products().getFirst().name()).isEqualTo("테스트 상품");
        assertThat(response.products().getFirst().colors()).extracting("name").containsExactly("화이트", "아이보리");
        assertThat(response.products().getFirst().colors()).extracting("value").containsExactly("#FFFFFF", "#FFF8E1");
        assertThat(response.products().getFirst().isLiked()).isFalse();
    }

    @Test
    @DisplayName("PRODUCT 타입 유사 상품 조회는 선택상품 기준으로 후보를 채우고 최대 4개를 반환한다")
    void getSimilarItems_productType_prioritizesFurnitureTypeAndLimitsToFour() {
        GenerateImage productImage = GenerateImage.builder()
                .id(1L)
                .generationType(GenerateImageType.PRODUCT)
                .build();

        CurationRawProduct selected = CurationRawProduct.builder()
                .id(101L)
                .productId(1001L)
                .brand("선택브랜드")
                .productName("선택 상품")
                .build();

        GenerateImageRawProduct mapping = GenerateImageRawProduct.of(productImage, selected, 1);

        FurnitureType furnitureType = FurnitureType.builder().id(1L).build();
        Furniture furniture = Furniture.builder().id(1L).furnitureType(furnitureType).build();
        Tag tag = Tag.builder().id(1L).build();
        FurnitureTag furnitureTag = FurnitureTag.builder().id(1L).furniture(furniture).tag(tag).build();
        CurationRawProductFurnitureTag selectedProductMapping = CurationRawProductFurnitureTag.builder()
                .id(1L)
                .furnitureTag(furnitureTag)
                .build();

        CurationRawProduct c1 = CurationRawProduct.builder().id(201L).productId(2001L).productName("furniture-type-1").build();
        CurationRawProduct c2 = CurationRawProduct.builder().id(202L).productId(2002L).productName("furniture-type-2").build();
        CurationRawProduct c3 = CurationRawProduct.builder().id(203L).productId(2003L).productName("furniture-type-3").build();
        CurationRawProduct c4 = CurationRawProduct.builder().id(204L).productId(2004L).productName("furniture-type-4").build();

        when(generateImageService.findGenerateImage(1L)).thenReturn(productImage);
        when(generateImageRawProductRepository.findAllByGenerateImageIdWithRawProduct(1L))
                .thenReturn(List.of(mapping));
        when(curationRawProductFurnitureTagRepository.findAllByCurationRawProductIdInWithFurnitureTag(List.of(101L)))
                .thenReturn(List.of(selectedProductMapping));
        when(curationRawProductRepository.findAllSimilarByFurnitureTypeIds(eq(List.of(1L)), eq(List.of(101L)), any()))
                .thenReturn(List.of(c1, c2, c3, c4));
        when(curationRawProductColorRepository.findAllByCurationRawProductIdIn(List.of(201L, 202L, 203L, 204L)))
                .thenReturn(List.of(
                        CurationRawProductColor.builder()
                                .id(11L)
                                .curationRawProduct(c1)
                                .clientColorName("오크")
                                .build(),
                        CurationRawProductColor.builder()
                                .id(12L)
                                .curationRawProduct(c2)
                                .rawColorName("블랙")
                                .build()
                ));
        when(recommendFurnitureRepository.findAllBySourceAndFurnitureProductIdIn(CurationSource.RAW, List.of(2001L, 2002L, 2003L, 2004L)))
                .thenReturn(List.of(
                        RecommendFurniture.builder().id(701L).source(CurationSource.RAW).furnitureProductId(2001L).build(),
                        RecommendFurniture.builder().id(702L).source(CurationSource.RAW).furnitureProductId(2002L).build(),
                        RecommendFurniture.builder().id(703L).source(CurationSource.RAW).furnitureProductId(2003L).build(),
                        RecommendFurniture.builder().id(704L).source(CurationSource.RAW).furnitureProductId(2004L).build()
                ));
        when(jjymRepository.findAllByUserIdAndRecommendFurnitureIdIn(1L, List.of(701L, 702L, 703L, 704L))).thenReturn(List.of());
        when(jjymRepository.countByRecommendFurnitureIds(List.of(701L, 702L, 703L, 704L)))
                .thenReturn(Map.of(701L, 4L, 702L, 2L));

        User user = User.builder().id(1L).build();
        SimilarItemsResponse response = generateImageResultService.getSimilarItems(user, 1L);

        assertThat(response.products()).hasSize(4);
        assertThat(response.products().get(0).id()).isEqualTo(201L);
        assertThat(response.products().get(3).id()).isEqualTo(204L);
        assertThat(response.products().get(0).colors()).extracting("name").containsExactly("오크");
        assertThat(response.products().get(1).colors()).extracting("name").containsExactly("블랙");
        assertThat(response.products().get(2).colors()).isEmpty();
        assertThat(response.products().get(0).isLiked()).isFalse();
        assertThat(response.products().get(0).jjymCount()).isEqualTo(4L);
        assertThat(response.products().get(1).jjymCount()).isEqualTo(2L);
        assertThat(response.products().get(2).jjymCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("related-images는 PRODUCT 타입 기준으로 현재 이미지를 제외하고 최신순/중복제거 결과를 반환한다")
    void getRelatedImages_returnsLatestDistinctImagesExcludingCurrent() {
        GenerateImage current = GenerateImage.builder()
                .id(1L)
                .generationType(GenerateImageType.PRODUCT)
                .build();

        CurationRawProduct selected = CurationRawProduct.builder()
                .id(101L)
                .productName("선택 상품")
                .build();
        GenerateImageRawProduct mapping = GenerateImageRawProduct.of(current, selected, 1);

        GenerateImage related1 = GenerateImage.builder()
                .id(200L)
                .url("https://image-200")
                .generationType(GenerateImageType.STYLE)
                .build();

        when(generateImageService.findGenerateImage(1L)).thenReturn(current);
        when(generateImageRawProductRepository.findAllByGenerateImageIdWithRawProduct(1L))
                .thenReturn(List.of(mapping));
        when(generateImageRepository.findRelatedImagesByRawProductIds(
                List.of(101L),
                1L,
                10,
                Set.of(GenerateImageType.BANNER, GenerateImageType.STYLE, GenerateImageType.PRODUCT)
        ))
                .thenReturn(List.of(related1));

        User user = mock(User.class);
        when(user.getDisplayName()).thenReturn("최윤하");

        RelatedImagesResponse response = generateImageResultService.getRelatedImages(user, 1L);

        assertThat(response.name()).isEqualTo("최윤하");
        assertThat(response.images()).hasSize(1);
        assertThat(response.images().get(0).id()).isEqualTo(200L);
        assertThat(response.images().get(0).resultType()).isEqualTo("STYLE");
    }
}
