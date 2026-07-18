package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.tag.infra.persistence.TagJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * #582 FurnitureTag→Tag 연관 절단(tag→tagId) 전, 매핑 DTO 동작을 고정하는 특성화 테스트.
 * 전환 전후 동일한 필드 값이 나와야 한다(furnitureTypeId/tagId/tagNameKr 등).
 */
class AdminCurationRawProductFurnitureTagResponseTest {

    @Test
    @DisplayName("of(): 매핑에서 가구·타입·태그 정보를 평탄화해 응답 필드로 채운다")
    void of_mapsAllFields() {
        // given
        FurnitureType furnitureType = FurnitureType.builder()
                .id(7L)
                .nameKr("소파")
                .nameEng("SOFA")
                .build();

        FurnitureJpaEntity furniture = FurnitureJpaEntity.builder()
                .id(3L)
                .furnitureNameKr("2인용 소파")
                .furnitureNameEng("TWO_SEATER_SOFA")
                .furnitureType(furnitureType)
                .build();

        TagJpaEntity tag = TagJpaEntity.builder()
                .id(5L)
                .tagNameKr("모던")
                .build();

        FurnitureTag furnitureTag = FurnitureTag.builder()
                .id(11L)
                .furniture(furniture)
                .tagId(tag.getId())
                .priority(2)
                .searchKeyword("모던 소파")
                .build();

        CurationRawProductFurnitureTag mapping = CurationRawProductFurnitureTag.builder()
                .id(99L)
                .furnitureTag(furnitureTag)
                .build();

        // when
        AdminCurationRawProductFurnitureTagResponse response =
                AdminCurationRawProductFurnitureTagResponse.of(mapping, tag.getTagNameKr());

        // then
        assertThat(response.mappingId()).isEqualTo(99L);
        assertThat(response.furnitureTagId()).isEqualTo(11L);
        assertThat(response.furnitureId()).isEqualTo(3L);
        assertThat(response.furnitureNameKr()).isEqualTo("2인용 소파");
        assertThat(response.furnitureTypeId()).isEqualTo(7L);
        assertThat(response.furnitureTypeNameKr()).isEqualTo("소파");
        assertThat(response.tagId()).isEqualTo(5L);
        assertThat(response.tagNameKr()).isEqualTo("모던");
        assertThat(response.priority()).isEqualTo(2);
        assertThat(response.searchKeyword()).isEqualTo("모던 소파");
    }
}
