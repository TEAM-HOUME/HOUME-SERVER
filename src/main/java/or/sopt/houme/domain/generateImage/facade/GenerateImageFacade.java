package or.sopt.houme.domain.generateImage.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.generateImage.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.openai.facade.OpenAiFacade;
import or.sopt.houme.domain.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.service.TasteTagService;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.GenerateImageException;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateImageFacade {

    private final GenerateImageService generateImageService;
    private final OpenAiFacade openAiFacade;
    private final HouseService houseService;
    private final CreditService creditService;
    private final TasteTagService tasteTagService;

    // 이미지 생성
    @Transactional
    public ImageInfoResponse generateImage(User user, GenerateImageRequest generateImageRequest){

            /**
             * redis 저장 (user랑 상태값)
             * 재요청시 user 조회 (상태값은 이미지 받았는지 판단)
             * 상태값이 요청 후 받았다( -> 재요청 가능 )
             */

            // 크레딧 감소
            creditService.decreaseCredit(user);

            Activity activity;
            try {
                    activity = Activity.valueOf(generateImageRequest.activity());
            } catch (IllegalArgumentException e){
                    throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }
            // 주요 활동 업데이트
            House house = houseService.updateHouseActivity(generateImageRequest.houseId(), activity);

            if (!house.getStructure().equals(Structure.DUPLEX)){
                generateImageRequest.selectiveIds().add(generateImageRequest.bedId());
            }

            // 가구 식별자 ID
            PromptFurnitureListDTO promptFurnitureListDTO = PromptFurnitureListDTO.of(generateImageRequest.selectiveIds());

            Equilibrium equilibrium;
            try {
                equilibrium = Equilibrium.valueOf(generateImageRequest.equilibrium());
            } catch (IllegalArgumentException e){
                throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }

            // 가장 우선순위가 높은 무드보드 id 제공
            Tag tag = tasteTagService.getPriorityId(generateImageRequest.moodBoardIds());

            PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(
                    generateImageRequest.floorPlan().floorPlanId(),
                    tag.getId(),
                    equilibrium,
                    promptFurnitureListDTO
            );

        try {

            // OpenAI로 image 생성
            ImageUploadResponseDTO imageUploadResponseDTO = openAiFacade.makeImage(promptRequestDTO);

            // 도면 이미지 생성
            GenerateImage generateImage = generateImageService.createGenerateImage(imageUploadResponseDTO, house);

            // 이미지 생성 여부 업데이트
            user.updateHasGeneratedImage();

            return ImageInfoResponse.of(generateImage.getId(), generateImage.getUrl(),
                    generateImageRequest.floorPlan().isMirror(),
                    generateImageRequest.equilibrium(), house.getForm().getDescription(),
                    tag.getTagNameKr(), user.getName());
        } catch (GenerateImageException e) {
          throw e;
        } catch (Exception e){
            log.info("Image 생성 중 오류 발생 {}", e.getMessage());
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        }
    }
}
