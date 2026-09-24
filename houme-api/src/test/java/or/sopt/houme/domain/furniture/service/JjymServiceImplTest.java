package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.compare.domain.EbayProduct;
import or.sopt.houme.compare.domain.port.out.EbayProductPort;
import or.sopt.houme.coupang.domain.CoupangProduct;
import or.sopt.houme.coupang.domain.port.out.CoupangProductPort;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;
import or.sopt.houme.furniture.domain.CurationRawProductColorView;
import or.sopt.houme.furniture.domain.CurationRawProductView;
import or.sopt.houme.furniture.domain.Jjym;
import or.sopt.houme.furniture.domain.RecommendFurniture;
import or.sopt.houme.furniture.domain.port.out.CurationRawProductQueryPort;
import or.sopt.houme.furniture.domain.port.out.JjymRepositoryPort;
import or.sopt.houme.furniture.domain.port.out.RecommendFurniturePort;
import or.sopt.houme.user.domain.User;
import or.sopt.houme.user.domain.port.out.UserRepositoryPort;
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

    private final JjymRepositoryPort jjymRepositoryPort = mock(JjymRepositoryPort.class);
    private final UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
    private final RecommendFurniturePort recommendFurniturePort = mock(RecommendFurniturePort.class);
    private final CurationRawProductQueryPort curationRawProductQueryPort = mock(CurationRawProductQueryPort.class);
    private final EbayProductPort compareCatalogPort = mock(EbayProductPort.class);
    private final CoupangProductPort coupangProductPort = mock(CoupangProductPort.class);

    private final JjymServiceImpl jjymService = new JjymServiceImpl(
            jjymRepositoryPort,
            userRepository,
            recommendFurniturePort,
            curationRawProductQueryPort,
            compareCatalogPort,
            coupangProductPort
    );

    @Test
    @DisplayName("raw product 기준 찜 토글 시 recommend furniture가 없으면 생성 후 찜 저장한다")
    void rawProductJjymToggle_createsRecommendFurnitureWhenMissing() {
        User user = User.builder().id(1L).build();
        CurationRawProductView rawProduct = CurationRawProductView.builder()
                .id(10L)
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
        given(curationRawProductQueryPort.findById(10L)).willReturn(Optional.of(rawProduct));
        given(recommendFurniturePort.findBySourceAndFurnitureProductId(CurationSource.RAW, 1000L))
                .willReturn(Optional.empty());
        given(recommendFurniturePort.save(any(RecommendFurniture.class))).willReturn(recommendFurniture);
        given(jjymRepositoryPort.findByUserIdAndRecommendFurnitureId(1L, 20L)).willReturn(Optional.empty());

        boolean result = jjymService.rawProductJjymToggle(1L, 10L);

        assertThat(result).isTrue();
        then(recommendFurniturePort).should().save(any(RecommendFurniture.class));
        then(jjymRepositoryPort).should().save(any(Jjym.class));
    }

    @Test
    @DisplayName("raw product 기반 찜 목록 조회 시 색상, 가격, 찜 개수를 포함해 반환한다")
    void getMyRawProductJjyms_returnsRawProductMetadata() {
        RecommendFurniture recommendFurniture = RecommendFurniture.builder()
                .id(20L)
                .furnitureProductId(1000L)
                .source(CurationSource.RAW)
                .furnitureProductImageUrl("https://recommend-image")
                .furnitureProductSiteUrl("https://recommend-site")
                .furnitureProductName("추천 소파")
                .build();
        Jjym jjym = Jjym.reconstitute(30L, 1L, 20L);
        CurationRawProductView rawProduct = CurationRawProductView.builder()
                .id(40L)
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
        CurationRawProductColorView firstColor = new CurationRawProductColorView(40L, "화이트", "오프화이트");
        CurationRawProductColorView secondColor = new CurationRawProductColorView(40L, null, "우드");

        given(jjymRepositoryPort.findAllByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(jjym));
        given(recommendFurniturePort.findAllByIdIn(List.of(20L))).willReturn(List.of(recommendFurniture));
        given(curationRawProductQueryPort.findAllByProductIdIn(List.of(1000L))).willReturn(List.of(rawProduct));
        given(curationRawProductQueryPort.findColorsByRawProductIdIn(List.of(40L)))
                .willReturn(List.of(firstColor, secondColor));
        given(jjymRepositoryPort.countByRecommendFurnitureIds(List.of(20L))).willReturn(Map.of(20L, 5L));

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

    @Test
    @DisplayName("source=COUPANG으로 찜 토글 시 RecommendFurniture가 없으면 생성 후 찜 저장한다")
    void catalogJjymToggle_coupang_createsRecommendFurnitureWhenMissing() {
        User user = User.builder().id(1L).build();
        CoupangProduct product = new CoupangProduct(5L, "쿠팡 의자", "https://img", "https://site", 30000L, 40000L, 25);
        RecommendFurniture rf = RecommendFurniture.builder().id(20L).furnitureProductId(5L).source(CurationSource.COUPANG).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(coupangProductPort.findById(5L)).willReturn(Optional.of(product));
        given(recommendFurniturePort.findBySourceAndFurnitureProductId(CurationSource.COUPANG, 5L)).willReturn(Optional.empty());
        given(recommendFurniturePort.save(any(RecommendFurniture.class))).willReturn(rf);
        given(jjymRepositoryPort.findByUserIdAndRecommendFurnitureId(1L, 20L)).willReturn(Optional.empty());

        boolean result = jjymService.catalogJjymToggle(1L, 5L, "COUPANG");

        assertThat(result).isTrue();
        then(recommendFurniturePort).should().save(any(RecommendFurniture.class));
        then(jjymRepositoryPort).should().save(any(Jjym.class));
    }

    @Test
    @DisplayName("source=COUPANG으로 찜 토글 시 이미 찜이면 삭제 후 false를 반환한다")
    void catalogJjymToggle_coupang_deletesWhenAlreadyJjym() {
        User user = User.builder().id(1L).build();
        CoupangProduct product = new CoupangProduct(5L, "쿠팡 의자", "https://img", "https://site", 30000L, 40000L, 25);
        RecommendFurniture rf = RecommendFurniture.builder().id(20L).furnitureProductId(5L).source(CurationSource.COUPANG).build();
        Jjym existing = Jjym.reconstitute(99L, 1L, 20L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(coupangProductPort.findById(5L)).willReturn(Optional.of(product));
        given(recommendFurniturePort.findBySourceAndFurnitureProductId(CurationSource.COUPANG, 5L)).willReturn(Optional.of(rf));
        given(jjymRepositoryPort.findByUserIdAndRecommendFurnitureId(1L, 20L)).willReturn(Optional.of(existing));

        boolean result = jjymService.catalogJjymToggle(1L, 5L, "COUPANG");

        assertThat(result).isFalse();
        then(jjymRepositoryPort).should().deleteById(99L);
    }

    @Test
    @DisplayName("쿠팡 찜 목록 조회 시 COUPANG source 항목이 포함된다")
    void getMyRawProductJjyms_returnsCoupangItem() {
        RecommendFurniture rf = RecommendFurniture.builder()
                .id(20L).furnitureProductId(5L).source(CurationSource.COUPANG)
                .furnitureProductImageUrl("https://fallback-img")
                .furnitureProductSiteUrl("https://fallback-site")
                .furnitureProductName("쿠팡 의자")
                .build();
        Jjym jjym = Jjym.reconstitute(30L, 1L, 20L);
        CoupangProduct product = new CoupangProduct(5L, "쿠팡 의자", "https://img", "https://site", 30000L, 40000L, 25);

        given(jjymRepositoryPort.findAllByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(jjym));
        given(recommendFurniturePort.findAllByIdIn(List.of(20L))).willReturn(List.of(rf));
        given(coupangProductPort.findAllByIdIn(List.of(5L))).willReturn(List.of(product));
        given(jjymRepositoryPort.countByRecommendFurnitureIds(List.of(20L))).willReturn(Map.of(20L, 3L));

        JjymV2ListResponse response = jjymService.getMyRawProductJjyms(1L);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).source()).isEqualTo("COUPANG");
        assertThat(response.items().get(0).catalogItemId()).isEqualTo(5L);
        assertThat(response.items().get(0).productName()).isEqualTo("쿠팡 의자");
        assertThat(response.items().get(0).listPrice()).isEqualTo(40000L);
        assertThat(response.items().get(0).discountRate()).isEqualTo(25);
        assertThat(response.items().get(0).discountPrice()).isEqualTo(30000L);
        assertThat(response.items().get(0).jjymCount()).isEqualTo(3L);
        assertThat(response.items().get(0).isJjym()).isTrue();
    }
}
