package or.sopt.houme.domain.generateImage.service.prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.TagException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
        List<Long> furnitureIds = promptFurnitureListDTO.furnitureTagIds();

        List<FurnitureTag> matchedFurnitureTags = furnitureTagRepository.findAllByFurnitureIdInAndTagId(furnitureIds, tagId.getId());
        log.info(
                "이미지 생성에 사용된 furniture_tag ids: {} (tagId={}, furnitureIds={})",
                matchedFurnitureTags.stream().map(FurnitureTag::getId).toList(),
                tagId.getId(),
                furnitureIds
        );

        List<String> furniturePrompts = matchedFurnitureTags.stream()
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

        log.info("event=image.prompt.created promptLength={}", finalPrompt.length());

        return finalPrompt;
    }
}
