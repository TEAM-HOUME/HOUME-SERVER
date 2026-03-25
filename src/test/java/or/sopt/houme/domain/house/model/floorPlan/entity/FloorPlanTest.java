package or.sopt.houme.domain.house.model.floorPlan.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImages;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.HouseException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloorPlanTest {

    @Test
    @DisplayName("create()는 floorPlanName이 null이면 INVALID_FLOOR_PLAN_NAME 예외를 던진다")
    void create_throwsWhenFloorPlanNameIsNull() {
        assertThatThrownBy(() -> FloorPlan.create(
                null,
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.BETWEEN_6_10,
                "prompt",
                sampleImages(),
                sampleImagesJson()
        ))
                .isInstanceOf(HouseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FLOOR_PLAN_NAME);
    }

    @Test
    @DisplayName("update()는 floorPlanName이 blank면 INVALID_FLOOR_PLAN_NAME 예외를 던진다")
    void update_throwsWhenFloorPlanNameIsBlank() {
        FloorPlan floorPlan = FloorPlan.create(
                "유효한 이름",
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.BETWEEN_6_10,
                "prompt",
                sampleImages(),
                sampleImagesJson()
        );

        assertThatThrownBy(() -> floorPlan.update(
                "   ",
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.BETWEEN_6_10,
                "updated prompt",
                sampleImages(),
                sampleImagesJson()
        ))
                .isInstanceOf(HouseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FLOOR_PLAN_NAME);
    }

    @Test
    @DisplayName("create()는 floorPlanName이 유효하면 엔티티를 생성한다")
    void create_succeedsWhenFloorPlanNameIsValid() {
        FloorPlan floorPlan = FloorPlan.create(
                "다용도실이 있는 원룸",
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.BETWEEN_6_10,
                "prompt",
                sampleImages(),
                sampleImagesJson()
        );

        assertThat(floorPlan.getFloorPlanName()).isEqualTo("다용도실이 있는 원룸");
    }

    private FloorPlanImages sampleImages() {
        return FloorPlanImages.from(List.of(
                FloorPlanImageItem.create(
                        "https://image/1",
                        "a.png",
                        "a-origin.png",
                        "png",
                        1,
                        "창가 뷰"
                )
        ));
    }

    private String sampleImagesJson() {
        return """
                [
                  {
                    "url": "https://image/1",
                    "filename": "a.png",
                    "originalFilename": "a-origin.png",
                    "fileExtension": "png",
                    "sortOrder": 1,
                    "view": "창가 뷰"
                  }
                ]
                """;
    }
}
