package or.sopt.houme.domain.banner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.presentation.dto.response.LandingListResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleDetailResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @InjectMocks
    private BannerServiceImpl bannerService;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private CurationRawProductColorRepository curationRawProductColorRepository;

    @Mock
    private RecommendFurnitureRepository recommendFurnitureRepository;

    @Mock
    private JjymRepository jjymRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("getLandings()는 LANDING 타입 이미지만 반환한다")
    void getLandings_returnsLandingTypeBanners() {
        Banner landing = Banner.create(
                BannerType.LANDING,
                "https://landing-image",
                "랜딩 제목",
                null,
                null,
                null,
                null,
                null
        );

        when(bannerRepository.findAllWithRawProducts(BannerType.LANDING, false)).thenReturn(List.of(landing));

        LandingListResponse response = bannerService.getLandings();

        assertThat(response.landings()).hasSize(1);
        assertThat(response.landings().getFirst().bannerId()).isNull();
        assertThat(response.landings().getFirst().name()).isEqualTo("랜딩 제목");
        assertThat(response.landings().getFirst().imageUrl()).isEqualTo("https://landing-image");
    }

    @Test
    @DisplayName("getOtherStyleDetail()는 스타일 상품의 색상 목록을 함께 반환한다")
    void getOtherStyleDetail_includesProductColors() {
        CurationRawProduct rawProduct = CurationRawProduct.builder()
                .id(100L)
                .productId(1000L)
                .productName("스타일 상품")
                .productImageUrl("https://image")
                .listPrice(10000L)
                .discountRate(10)
                .discountPrice(9000L)
                .productSiteUrl("https://link")
                .build();

        Banner style = Banner.builder()
                .id(1L)
                .bannerType(BannerType.STYLE)
                .bannerTitle("모던 스타일")
                .bannerImageUrl("https://style")
                .styleDescription("스타일 설명")
                .bannerRawProducts(new java.util.ArrayList<>())
                .build();
        style.getBannerRawProducts().add(BannerCurationRawProduct.of(style, rawProduct));

        when(bannerRepository.findByIdWithRawProducts(1L, BannerType.STYLE, false)).thenReturn(Optional.of(style));
        when(curationRawProductColorRepository.findAllByCurationRawProductIdIn(List.of(100L)))
                .thenReturn(List.of(
                        CurationRawProductColor.builder()
                                .id(1L)
                                .curationRawProduct(rawProduct)
                                .clientColorName("화이트")
                                .build(),
                        CurationRawProductColor.builder()
                                .id(2L)
                                .curationRawProduct(rawProduct)
                                .rawColorName("우드")
                                .build()
                ));
        when(recommendFurnitureRepository.findAllBySourceAndFurnitureProductIdIn(CurationSource.RAW, List.of(1000L)))
                .thenReturn(List.of(RecommendFurniture.builder()
                        .id(1L)
                        .source(CurationSource.RAW)
                        .furnitureProductId(1000L)
                        .build()));
        when(jjymRepository.findAllByUserIdAndRecommendFurnitureIdIn(1L, List.of(1L))).thenReturn(List.of());

        or.sopt.houme.domain.user.model.entity.User user = or.sopt.houme.domain.user.model.entity.User.builder().id(1L).build();
        OtherStyleDetailResponse response = bannerService.getOtherStyleDetail(user, 1L);

        assertThat(response.products()).hasSize(1);
        assertThat(response.products().getFirst().colors()).extracting("name").containsExactly("화이트", "우드");
        assertThat(response.products().getFirst().colors()).extracting("value").containsExactly("#FFFFFF", "#A67B5B");
        assertThat(response.products().getFirst().isLiked()).isFalse();
    }
}
