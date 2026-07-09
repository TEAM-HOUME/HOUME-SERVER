package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class JjymServiceImplTest {

    private final JjymRepository jjymRepository = mock(JjymRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RecommendFurnitureRepository recommendFurnitureRepository = mock(RecommendFurnitureRepository.class);
    private final CurationRawProductRepository curationRawProductRepository = mock(CurationRawProductRepository.class);
    private final CurationRawProductColorRepository curationRawProductColorRepository = mock(CurationRawProductColorRepository.class);

    private final JjymServiceImpl jjymService = new JjymServiceImpl(
            jjymRepository,
            userRepository,
            recommendFurnitureRepository,
            curationRawProductRepository,
            curationRawProductColorRepository
    );

    @Test
    @DisplayName("raw product 기준 찜 토글 시 recommend furniture가 없으면 생성 후 찜 저장한다")
    void rawProductJjymToggle_createsRecommendFurnitureWhenMissing() {
        User user = User.builder().id(1L).build();
        CurationRawProduct rawProduct = CurationRawProduct.builder()
                .id(10L)
                .source("soozip")
                .category(SoozipCategory.FURNITURE)
                .productId(1000L)
                .productImageUrl("https://image")
                .productSiteUrl("https://site")
                .productName("소파")
                .productMallName("수집몰")
                .fetchedAt(LocalDateTime.now())
                .build();
        RecommendFurniture recommendFurniture = RecommendFurniture.builder()
                .id(20L)
                .furnitureProductId(1000L)
                .source(CurationSource.RAW)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(curationRawProductRepository.findById(10L)).willReturn(Optional.of(rawProduct));
        given(recommendFurnitureRepository.findBySourceAndFurnitureProductId(CurationSource.RAW, 1000L))
                .willReturn(Optional.empty());
        given(recommendFurnitureRepository.save(any(RecommendFurniture.class))).willReturn(recommendFurniture);
        given(jjymRepository.findByUserIdAndRecommendFurnitureId(1L, 20L)).willReturn(Optional.empty());

        boolean result = jjymService.rawProductJjymToggle(1L, 10L);

        assertThat(result).isTrue();
        then(recommendFurnitureRepository).should().save(any(RecommendFurniture.class));
        then(jjymRepository).should().save(any(Jjym.class));
    }

    @Test
    @DisplayName("raw product 기반 찜 목록 조회 시 색상, 가격, 찜 개수를 포함해 반환한다")
    void getMyRawProductJjyms_returnsRawProductMetadata() {
        User user = User.builder().id(1L).build();
        RecommendFurniture recommendFurniture = RecommendFurniture.builder()
                .id(20L)
                .furnitureProductId(1000L)
                .source(CurationSource.RAW)
                .furnitureProductImageUrl("https://recommend-image")
                .furnitureProductSiteUrl("https://recommend-site")
                .furnitureProductName("추천 소파")
                .build();
        Jjym jjym = Jjym.builder()
                .id(30L)
                .user(user)
                .recommendFurniture(recommendFurniture)
                .build();
        CurationRawProduct rawProduct = CurationRawProduct.builder()
                .id(40L)
                .source("soozip")
                .category(SoozipCategory.FURNITURE)
                .productId(1000L)
                .productImageUrl("https://raw-image")
                .productSiteUrl("https://raw-site")
                .productName("패브릭 소파")
                .brand("브랜드A")
                .listPrice(100000L)
                .discountRate(20)
                .discountPrice(80000L)
                .productMallName("수집몰")
                .fetchedAt(LocalDateTime.now())
                .build();
        CurationRawProductColor firstColor = CurationRawProductColor.builder()
                .curationRawProduct(rawProduct)
                .rawColorName("오프화이트")
                .clientColorName("화이트")
                .build();
        CurationRawProductColor secondColor = CurationRawProductColor.builder()
                .curationRawProduct(rawProduct)
                .rawColorName("우드")
                .clientColorName(null)
                .build();

        given(jjymRepository.findAllByUserIdWithFurnitureOrderByCreatedAtDesc(1L)).willReturn(List.of(jjym));
        given(curationRawProductRepository.findAllByProductIdIn(List.of(1000L))).willReturn(List.of(rawProduct));
        given(curationRawProductColorRepository.findAllByCurationRawProductIdIn(List.of(40L)))
                .willReturn(List.of(firstColor, secondColor));
        given(jjymRepository.countByRecommendFurnitureIds(List.of(20L))).willReturn(Map.of(20L, 5L));

        JjymV2ListResponse response = jjymService.getMyRawProductJjyms(1L);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).rawProductId()).isEqualTo(40L);
        assertThat(response.items().get(0).productImageUrl()).isEqualTo("https://raw-image");
        assertThat(response.items().get(0).productSiteUrl()).isEqualTo("https://raw-site");
        assertThat(response.items().get(0).colors()).containsExactly("화이트", "우드");
        assertThat(response.items().get(0).brandName()).isEqualTo("브랜드A");
        assertThat(response.items().get(0).productName()).isEqualTo("패브릭 소파");
        assertThat(response.items().get(0).listPrice()).isEqualTo(100000L);
        assertThat(response.items().get(0).discountRate()).isEqualTo(20);
        assertThat(response.items().get(0).discountPrice()).isEqualTo(80000L);
        assertThat(response.items().get(0).jjymCount()).isEqualTo(5L);
        assertThat(response.items().get(0).isJjym()).isTrue();
    }
}
