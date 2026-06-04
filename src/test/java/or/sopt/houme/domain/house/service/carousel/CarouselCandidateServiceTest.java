package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.house.service.carousel.dto.CarouselCandidateBundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarouselCandidateServiceTest {

    @InjectMocks
    private CarouselCandidateService carouselCandidateService;

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Test
    @DisplayName("후보군 조회 시 이전 버킷의 상품 ID를 excludedIds로 누적 전달한다")
    void collectCandidates_accumulatesExcludedIdsAcrossBuckets() {
        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByFurnitureIds(
                1L,
                List.of(10L, 20L),
                SoozipCategory.FURNITURE,
                160,
                List.of()
        )).thenReturn(List.of(rawProduct(101L)));
        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                1L,
                SoozipCategory.FURNITURE,
                160,
                List.of(101L)
        )).thenReturn(List.of(rawProduct(102L)));
        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                eq(1L),
                eq(SoozipCategory.LIGHTING),
                eq(60),
                eq(List.of(101L, 102L))
        )).thenReturn(List.of(rawProduct(103L)));
        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                eq(1L),
                eq(SoozipCategory.LIVING_GOODS),
                eq(60),
                eq(List.of(101L, 102L, 103L))
        )).thenReturn(List.of(rawProduct(104L)));
        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                eq(1L),
                eq(SoozipCategory.HOME_FABRIC),
                eq(60),
                eq(List.of(101L, 102L, 103L, 104L))
        )).thenReturn(List.of());
        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                eq(1L),
                eq(SoozipCategory.ACCESSORY),
                eq(60),
                eq(List.of(101L, 102L, 103L, 104L))
        )).thenReturn(List.of());
        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                eq(1L),
                eq(SoozipCategory.MINI_ELECTRONICS),
                eq(60),
                eq(List.of(101L, 102L, 103L, 104L))
        )).thenReturn(List.of());
        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                1L,
                null,
                220,
                List.of(101L, 102L, 103L, 104L)
        )).thenReturn(List.of(rawProduct(105L)));

        CarouselCandidateBundle result = carouselCandidateService.collectCandidates(1L, List.of(10L, 20L));

        assertThat(result.selectedFurnitureIds()).containsExactly(101L);
        assertThat(result.furnitureCategoryIds()).containsExactly(102L);
        assertThat(result.otherCategoryIds().get(SoozipCategory.LIGHTING)).containsExactly(103L);
        assertThat(result.otherCategoryIds().get(SoozipCategory.LIVING_GOODS)).containsExactly(104L);
        assertThat(result.fallbackIds()).containsExactly(105L);
    }

    private CurationRawProduct rawProduct(Long id) {
        return CurationRawProduct.builder()
                .id(id)
                .productImageUrl("image-" + id)
                .build();
    }
}
