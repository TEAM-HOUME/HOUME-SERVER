package or.sopt.houme.domain.prompt.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.floorPlan.repository.FloorPlanRepository;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.TagException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final FloorPlanRepository floorPlanRepository;
    private final FurnitureTagRepository furnitureTagRepository;
    private final TagRepository tagRepository;

    @Override
    public String makePrompt(PromptRequestDTO requestDTO) {

        // 도면 프롬프트 가져오기
        FloorPlan floorPlanId = floorPlanRepository.findById(requestDTO.floorPlanId())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FLOOR_PLAN));
        String floorPlanPrompt = floorPlanId.getFloorPlanPrompt();

        // 평형 프롬프트 가져오기
        Equilibrium equilibrium = requestDTO.equilibrium();
        String equilibriumPrompt = equilibrium.getDescription();

        // 취향 프롬프트 가져오기
        Tag tagId = tagRepository.findById(requestDTO.tagId())
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));
        String tagPrompt = tagId.getTagPrompt();


        // 가구 프롬프트 가져오기
        PromptFurnitureListDTO promptFurnitureListDTO = requestDTO.promptFurnitureListDTO();
        List<Long> furnitureIds = promptFurnitureListDTO.furnitureIds();

        List<String> furniturePrompts = furnitureTagRepository.findAllById(furnitureIds).stream()
                .map(FurnitureTag::getFurniturePrompt)
                .toList();

        // 줄바꿈으로 이어붙이기
        String joinedFurniturePrompt = String.join("\n", furniturePrompts);

        // 최종 프롬프트 조합
        String finalPrompt = makeFinalPrompt(floorPlanPrompt, equilibriumPrompt, tagPrompt, joinedFurniturePrompt);

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
