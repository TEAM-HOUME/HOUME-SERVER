package or.sopt.houme.domain.prompt.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.floorPlan.repository.FloorPlanRepository;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.openai.service.OpenAiService;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final FloorPlanRepository floorPlanRepository;

    @Override
    public String makePrompt(PromptRequestDTO requestDTO) {

        // 도면 프롬프트 가져오기
        FloorPlan floorPlanId = floorPlanRepository.getReferenceById(requestDTO.floorPlanId());
        String floorPlanPrompt = floorPlanId.getFloorPlanPrompt();

        // 평형 프롬프트 가져오기
        Equilibrium equilibrium = requestDTO.equilibrium();
        String equilibriumPrompt = equilibrium.getDescription();

        // 취향 프롬프트 가져오기


        // 가구 프롬프트 가져오기

        return "웃는 남자의 이미지를 뽑아줘";
    }
}
