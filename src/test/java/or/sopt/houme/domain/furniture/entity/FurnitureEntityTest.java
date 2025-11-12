package or.sopt.houme.domain.furniture.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.sopt.houme.domain.admin.controller.dto.furniture.AdminFurnitureRequestDTO;

import static org.assertj.core.api.Assertions.assertThat;

class FurnitureEntityTest {

    @Test
    @DisplayName("createByAdminFurnitureRequestDTO는 영어명을 대문자+언더스코어로 정규화한다")
    void create_normalizesEnglishName() {
        // given
        FurnitureType type = FurnitureType.builder().id(1L).nameKr("의자").nameEng("CHAIR").build();
        AdminFurnitureRequestDTO dto = new AdminFurnitureRequestDTO("의자", " test chair ", 1L);

        // when
        Furniture furniture = Furniture.createByAdminFurnitureRequestDTO(dto, type);

        // then
        assertThat(furniture.getFurnitureNameEng()).isEqualTo("TEST_CHAIR");
    }

    @Test
    @DisplayName("updateFurnitureNameEng는 영어명을 대문자+언더스코어로 정규화한다")
    void update_normalizesEnglishName() {
        // given
        Furniture furniture = Furniture.builder().furnitureNameKr("테이블").build();

        // when
        furniture.updateFurnitureNameEng("new table name");

        // then
        assertThat(furniture.getFurnitureNameEng()).isEqualTo("NEW_TABLE_NAME");
    }
}

