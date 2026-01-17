package or.sopt.houme.domain.generateImage.service.prompt;

import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceImplTest {


    @Mock
    private FloorPlanRepository floorPlanRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private FurnitureTagRepository furnitureTagRepository;

    @InjectMocks
    private PromptServiceImpl promptService;


    @Test
    @DisplayName("makePrompt()는 floorPlan, equilibrium, taste, furniture를 이용해 문자열을 반환한다")
    void makePrompt_success() {
        // given
        Long floorPlanId = 1L;
        Long tasteId = 2L;
        Tag tag = Tag.builder()
                .id(3L)
                .tagPrompt("취향 프롬프트")
                .build();
        List<Long> furnitureIds = List.of(10L, 20L);

        PromptRequestDTO requestDTO = PromptRequestDTO.of(
                floorPlanId,
                tasteId,
                Equilibrium.UNDER_5,
                new PromptFurnitureListDTO(furnitureIds)
        );

        when(floorPlanRepository.findById(floorPlanId))
                .thenReturn(Optional.of(FloorPlan.builder().floorPlanPrompt("도면 프롬프트").build()));

        when(tagRepository.findById(tasteId))
                .thenReturn(Optional.of(tag));

        FurnitureTag furnitureTag1 = FurnitureTag.builder().furniturePrompt("침대").build();
        FurnitureTag furnitureTag2 = FurnitureTag.builder().furniturePrompt("책상").build();

        when(furnitureTagRepository.findAllByFurnitureIdInAndTagId(furnitureIds, tag.getId()))
                .thenReturn(List.of(furnitureTag1, furnitureTag2));

        // when
        String result = promptService.makePrompt(requestDTO);

        // then
        assertThat(result).isEqualTo(
                "도면 프롬프트\n" +
                        Equilibrium.UNDER_5.getDescription() + "\n" +
                        "취향 프롬프트\n" +
                        "침대\n책상"
        );
    }
}