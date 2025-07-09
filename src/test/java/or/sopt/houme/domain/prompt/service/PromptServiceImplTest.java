package or.sopt.houme.domain.prompt.service;

import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.floorPlan.repository.FloorPlanRepository;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.domain.taste.repository.TasteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceImplTest {


    @Mock
    private FloorPlanRepository floorPlanRepository;

    @Mock
    private TasteRepository tasteRepository;

    @Mock
    private FurnitureRepository furnitureRepository;

    @InjectMocks
    private PromptServiceImpl promptService;


    @Test
    @DisplayName("makePrompt()는 floorPlan, equilibrium, taste, furniture를 이용해 문자열을 반환한다")
    void makePrompt_success() {
        // given
        Long floorPlanId = 1L;
        Long tasteId = 2L;
        List<Long> furnitureIds = List.of(10L, 20L);

        PromptRequestDTO requestDTO = PromptRequestDTO.of(
                floorPlanId,
                tasteId,
                Equilibrium.UNDER_5,
                new PromptFurnitureListDTO(furnitureIds)
        );

        when(floorPlanRepository.getReferenceById(floorPlanId))
                .thenReturn(FloorPlan.builder().floorPlanPrompt("도면 프롬프트").build());

        when(tasteRepository.getReferenceById(tasteId))
                .thenReturn(Taste.builder().tastePrompt("취향 프롬프트").build());

        Furniture furniture1 = Furniture.builder().furniturePrompt("침대").build();
        Furniture furniture2 = Furniture.builder().furniturePrompt("책상").build();

        when(furnitureRepository.findAllById(furnitureIds))
                .thenReturn(List.of(furniture1, furniture2));

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