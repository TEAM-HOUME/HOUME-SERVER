package or.sopt.houme.domain.generateImage.facade;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.openai.facade.OpenAiFacade;
import or.sopt.houme.domain.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerateImageFacade {

    private final GenerateImageService generateImageService;
    private final OpenAiFacade openAiFacade;
    private final HouseService houseService;

    // 이미지 생성
    public ImageInfoResponse generateImage(GenerateImageRequest generateImageRequest){

        // update Activity
        House house = houseService.updateHouseActivity
                (generateImageRequest.houseId(), Activity.valueOf(generateImageRequest.activity()));

        // 가구 식별자 ID
        PromptFurnitureListDTO promptFurnitureListDTO = PromptFurnitureListDTO.of(generateImageRequest.selectiveIds());

        PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(
                generateImageRequest.floorPlan().floorPlanId(),
                generateImageRequest.floorPlan().isMirror(),
                generateImageRequest.moodBoardId(),
                Equilibrium.valueOf(generateImageRequest.equilibrium()),
                promptFurnitureListDTO
        );

        // OpenAI로 image 생성
        ImageUploadResponseDTO imageUploadResponseDTO = openAiFacade.makeImage(promptRequestDTO);

        // 도면 이미지 생성
        GenerateImage generateImage = generateImageService.createGenerateImage(imageUploadResponseDTO, house);

        return ImageInfoResponse.of(generateImage.getId(), generateImage.getUrl());
    }
}
