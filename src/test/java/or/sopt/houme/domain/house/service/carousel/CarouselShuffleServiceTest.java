package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.house.service.carousel.dto.CarouselCandidateBundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CarouselShuffleServiceTest {

    private final CarouselShuffleService carouselShuffleService = new CarouselShuffleService();

    @Test
    @DisplayName("메인 조립은 선택 가구 4 대 일반 가구 1 비율을 우선 적용하고 기타 카테고리는 부족할 때만 사용한다")
    void selectDisplayIds_prefersSelectedAndFurnitureBeforeOtherCategories() {
        Map<SoozipCategory, List<Long>> otherCategoryIds = new LinkedHashMap<>();
        otherCategoryIds.put(SoozipCategory.LIGHTING, List.of(1001L, 1002L));

        CarouselCandidateBundle bundle = new CarouselCandidateBundle(
                null,
                List.of(1L, 2L, 3L, 4L),
                List.of(101L),
                otherCategoryIds,
                List.of(2001L, 2002L)
        );

        List<Long> result = carouselShuffleService.selectDisplayIds(bundle, 1L);

        assertThat(result.subList(0, 5)).containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 101L);
        assertThat(result.subList(5, 7)).containsExactlyInAnyOrder(1001L, 1002L);
        assertThat(result.subList(7, 9)).containsExactlyInAnyOrder(2001L, 2002L);
    }
}
