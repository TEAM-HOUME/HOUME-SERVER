package or.sopt.houme.domain.prompt.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.floorPlan.repository.FloorPlanRepository;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.domain.taste.repository.taste.TasteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final FloorPlanRepository floorPlanRepository;
    private final TasteRepository tasteRepository;
    private final FurnitureRepository furnitureRepository;
    private final FurnitureTagRepository furnitureTagRepository;

    @Override
    public String makePrompt(PromptRequestDTO requestDTO) {

        // 도면 프롬프트 가져오기
        FloorPlan floorPlanId = floorPlanRepository.getReferenceById(requestDTO.floorPlanId());
        String floorPlanPrompt = floorPlanId.getFloorPlanPrompt();

        // 평형 프롬프트 가져오기
        Equilibrium equilibrium = requestDTO.equilibrium();
        String equilibriumPrompt = equilibrium.getDescription();

        // 취향 프롬프트 가져오기
        Taste tasteId = tasteRepository.getReferenceById(requestDTO.tasteId());
        String tastePrompt = tasteId.getTastePrompt();

        // 가구 프롬프트 가져오기
        PromptFurnitureListDTO promptFurnitureListDTO = requestDTO.promptFurnitureListDTO();
        List<Long> furnitureIds = promptFurnitureListDTO.furnitureIds();

        List<String> furniturePrompts = furnitureTagRepository.findAllById(furnitureIds).stream()
                .map(FurnitureTag::getFurniturePrompt)
                .toList();

        // 줄바꿈으로 이어붙이기
        String joinedFurniturePrompt = String.join("\n", furniturePrompts);

        // 최종 프롬프트 조합
        String finalPrompt = makeFinalPrompt(floorPlanPrompt, equilibriumPrompt, tastePrompt, joinedFurniturePrompt);

        return finalPrompt;
    }



    private static String makeFinalPrompt(String floorPlanPrompt, String equilibriumPrompt, String tastePrompt, String joinedFurniturePrompt) {
        String finalPrompt = floorPlanPrompt + "\n"
                + equilibriumPrompt + "\n"
                + tastePrompt + "\n"
                + joinedFurniturePrompt;

        return finalPrompt;
    }
}
