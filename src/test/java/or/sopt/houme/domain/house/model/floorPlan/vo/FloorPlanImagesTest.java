package or.sopt.houme.domain.house.model.floorPlan.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.sopt.houme.global.api.GeneralException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloorPlanImagesTest {

    @Test
    @DisplayName("from()은 sortOrder 기준으로 이미지를 정렬하고 첫 이미지를 대표로 선택한다")
    void from_sortsAndSelectsRepresentative() {
        FloorPlanImages images = FloorPlanImages.from(List.of(
                FloorPlanImageItem.create("https://image/2", "b.png", "b-origin.png", "png", 2, "창가 뷰"),
                FloorPlanImageItem.create("https://image/1", "a.png", "a-origin.png", "png", 1, "복도 뷰")
        ));

        assertThat(images.items()).extracting(FloorPlanImageItem::sortOrder).containsExactly(1, 2);
        assertThat(images.representative().url()).isEqualTo("https://image/1");
    }

    @Test
    @DisplayName("from()은 중복된 sortOrder를 거절한다")
    void from_rejectsDuplicatedSortOrder() {
        assertThatThrownBy(() -> FloorPlanImages.from(List.of(
                FloorPlanImageItem.create("https://image/1", "a.png", "a-origin.png", "png", 1, "창가 뷰"),
                FloorPlanImageItem.create("https://image/2", "b.png", "b-origin.png", "png", 1, "복도 뷰")
        )))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("restore()는 JSON 이미지가 없으면 기존 대표 이미지를 복원한다")
    void restore_usesLegacyImageWhenJsonImagesAbsent() {
        FloorPlanImageItem legacy = FloorPlanImageItem.create("https://legacy", "legacy.png", "legacy-origin.png", "png", 1, null);

        FloorPlanImages restored = FloorPlanImages.restore(List.of(), legacy);

        assertThat(restored.items()).hasSize(1);
        assertThat(restored.representative().url()).isEqualTo("https://legacy");
    }
}
