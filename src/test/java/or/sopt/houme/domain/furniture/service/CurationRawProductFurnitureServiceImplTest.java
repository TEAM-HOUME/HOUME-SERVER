package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureProductsInfoResponseV2;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductFurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurationRawProductFurniture 서비스 단위 테스트")
class CurationRawProductFurnitureServiceImplTest {

    @InjectMocks
    private CurationRawProductFurnitureServiceImpl service;

    @Mock
    private CurationRawProductFurnitureRepository curationRawProductFurnitureRepository;

    @Mock
    private CurationRawProductColorRepository curationRawProductColorRepository;

    @Mock
    private RecommendFurnitureRepository recommendFurnitureRepository;

    @Mock
    private JjymRepository jjymRepository;

    @Mock
    private FurnitureRepository furnitureRepository;

    @Test
    @DisplayName("getFurnitureIdsHavingProducts()는 레포지토리 결과를 그대로 반환한다")
    void getFurnitureIdsHavingProducts() {
        given(curationRawProductFurnitureRepository.findFurnitureIdsHavingProducts(List.of(1L, 2L, 3L)))
                .willReturn(List.of(1L, 3L));

        List<Long> result = service.getFurnitureIdsHavingProducts(List.of(1L, 2L, 3L));

        assertThat(result).containsExactly(1L, 3L);
    }

    @Test
    @DisplayName("buildProductsResponseByFurnitureId()는 매핑 상품이 없으면 빈 products를 반환한다")
    void buildProductsResponseByFurnitureId_emptyWhenNoMappings() {
        Long furnitureId = 1L;
        User user = User.builder().id(1L).name("테스트").build();
        Furniture furniture = Furniture.builder().id(furnitureId).furnitureNameKr("소파").build();

        given(furnitureRepository.findById(furnitureId)).willReturn(Optional.of(furniture));
        given(curationRawProductFurnitureRepository.findExposedByFurnitureId(furnitureId))
                .willReturn(List.of());

        FurnitureProductsInfoResponseV2 response = service.buildProductsResponseByFurnitureId(user, furnitureId);

        assertThat(response.products()).isEmpty();
        assertThat(response.userName()).isEqualTo("테스트");
    }

    @Test
    @DisplayName("buildProductsResponseByFurnitureId()는 매핑 상품의 카테고리명을 가구명으로 채운다")
    void buildProductsResponseByFurnitureId_setsCategoryNameFromFurniture() {
        Long furnitureId = 1L;
        User user = User.builder().id(1L).name("홍길동").build();
        Furniture furniture = Furniture.builder().id(furnitureId).furnitureNameKr("소파").build();

        CurationRawProduct rawProduct = CurationRawProduct.builder()
                .id(100L)
                .productId(9999L)
                .productName("모던 소파")
                .productImageUrl("https://img.example.com/sofa.jpg")
                .productSiteUrl("https://store.example.com/sofa")
                .source("raw")
                .discountPrice(150000L)
                .isExposed(true)
                .build();

        CurationRawProductFurniture mapping = CurationRawProductFurniture.builder()
                .curationRawProduct(rawProduct)
                .furniture(furniture)
                .build();

        given(furnitureRepository.findById(furnitureId)).willReturn(Optional.of(furniture));
        given(curationRawProductFurnitureRepository.findExposedByFurnitureId(furnitureId))
                .willReturn(List.of(mapping));
        given(curationRawProductColorRepository.findAllByCurationRawProductIdIn(List.of(100L)))
                .willReturn(List.of());
        given(recommendFurnitureRepository.findAllBySourceAndFurnitureProductIdIn(any(), any()))
                .willReturn(List.of());

        FurnitureProductsInfoResponseV2 response = service.buildProductsResponseByFurnitureId(user, furnitureId);

        assertThat(response.products()).hasSize(1);
        assertThat(response.products().get(0).product().categoryName()).isEqualTo("소파");
        assertThat(response.products().get(0).product().name()).isEqualTo("모던 소파");
    }

    @Test
    @DisplayName("buildProductsResponseByFurnitureId()는 가구가 없으면 예외를 던진다")
    void buildProductsResponseByFurnitureId_throwsWhenFurnitureNotFound() {
        User user = User.builder().id(1L).build();
        given(furnitureRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.buildProductsResponseByFurnitureId(user, 99L))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("buildProductsResponseByFurnitureId()는 user가 null이면 isLiked false로 반환한다")
    void buildProductsResponseByFurnitureId_noLikedWhenUserNull() {
        Long furnitureId = 1L;
        Furniture furniture = Furniture.builder().id(furnitureId).furnitureNameKr("침대").build();

        CurationRawProduct rawProduct = CurationRawProduct.builder()
                .id(200L)
                .productId(8888L)
                .productName("킹침대")
                .productImageUrl("https://img.example.com/bed.jpg")
                .productSiteUrl("https://store.example.com/bed")
                .source("raw")
                .discountPrice(300000L)
                .isExposed(true)
                .build();

        CurationRawProductFurniture mapping = CurationRawProductFurniture.builder()
                .curationRawProduct(rawProduct)
                .furniture(furniture)
                .build();

        given(furnitureRepository.findById(furnitureId)).willReturn(Optional.of(furniture));
        given(curationRawProductFurnitureRepository.findExposedByFurnitureId(furnitureId))
                .willReturn(List.of(mapping));
        given(curationRawProductColorRepository.findAllByCurationRawProductIdIn(List.of(200L)))
                .willReturn(List.of());

        FurnitureProductsInfoResponseV2 response = service.buildProductsResponseByFurnitureId(null, furnitureId);

        assertThat(response.products()).hasSize(1);
        assertThat(response.products().get(0).product().isLiked()).isFalse();
    }
}
