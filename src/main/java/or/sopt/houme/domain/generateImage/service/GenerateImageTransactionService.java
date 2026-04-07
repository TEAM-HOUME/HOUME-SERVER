package or.sopt.houme.domain.generateImage.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.response.BannerGenerateImageResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.HouseFloorPlanRepository;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.house.presentation.taste.dto.response.TagDTO;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.HouseException;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateImageTransactionService {

    private final CreditService creditService;
    private final HouseService houseService;
    private final HouseFloorPlanRepository houseFloorPlanRepository;
    private final GenerateImageService generateImageService;
    private final UserService userService;

    // DB 관련 로직을 위한 별도의 @Transactional 메서드 생성
    @Transactional
    public List<ImageInfoResponse> saveResultsAndCreateResponse(
            User user, House house, List<ImageUploadResponseDTO> results,
            GenerateImageRequest generateImageRequest, List<TagDTO> priorityIdList, Credit credit,
            GenerateImageType generationType) {

        // 크레딧 차감 로직
        creditService.commitCreditDeletion(credit);

        // house에 프롬프트 저장
        for (ImageUploadResponseDTO result : results) {
            houseService.saveHousePrompt(house, result.getPullPrompt());
        }

        // 도면 이미지 생성 및 저장
        List<GenerateImage> generateImages = results.stream()
                .map(result -> generateImageService.createGenerateImage(
                        result,
                        house,
                        generationType
                ))
                .toList();

        // 사용자 계정 이미지 생성여부 업데이트
        userService.updateHasGeneratedImage(user);

        // 반환 리스트 생성
        FloorPlan floorPlan = getFloorPlanOrThrow(house);
        List<ImageInfoResponse> imageInfoResponses = new ArrayList<>();
        for (int i = 0; i < generateImages.size(); i++) {
            imageInfoResponses.add(
                    ImageInfoResponse.of(generateImages.get(i).getId(), generateImages.get(i).getUrl(),
                            generateImageRequest.floorPlan().isMirror(),
                            floorPlan.getEquilibrium().getDescription(), floorPlan.getForm().getDescription(),
                            priorityIdList.get(i).tagNameKr(), user.getName())
            );
        }
        return imageInfoResponses;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 확실하게 분리된 트랜잭션 보장
    public ImageInfoResponse saveAllDataAndConfirmCredit(
            User user,
            Credit lockedCredit,
            GenerateImageRequest request,
            ImageUploadResponseDTO imageResponse,
            Tag priorityTag,
            Activity activity,
            GenerateImageType generationType
    ) {
        // 1. House 정보 업데이트
        House house = houseService.updateHouseActivity(request.houseId(), activity);

        // 2. 가구 및 무드보드, 프롬프트 저장
        houseService.saveHouseFloorPlan(house, request.floorPlan().floorPlanId(),request.floorPlan().isMirror());
        houseService.saveHouseFurniture(house, request.selectiveIds());
        houseService.saveHouseTaste(house, request.moodBoardIds());
        houseService.saveHousePrompt(house, imageResponse.getPullPrompt());

        // 3. 이미지 엔티티 생성 및 저장
        GenerateImage generateImage = generateImageService.createGenerateImage(
                imageResponse,
                house,
                generationType
        );

        // 4. 크레딧 차감 확정 (PENDING -> DELETE)
        creditService.commitCreditDeletion(lockedCredit);

        // 5. 유저 상태 업데이트
        userService.updateHasGeneratedImage(user);

        // 6. 응답 DTO 생성
        FloorPlan floorPlan = getFloorPlanOrThrow(house);
        return ImageInfoResponse.of(
                generateImage.getId(),
                generateImage.getUrl(),
                request.floorPlan().isMirror(),
                floorPlan.getEquilibrium().getDescription(),
                floorPlan.getForm().getDescription(),
                priorityTag.getTagNameKr(),
                user.getName()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BannerGenerateImageResponse saveBannerImageAndConfirmCredit(
            User user,
            Credit lockedCredit,
            Banner banner,
            Long floorPlanId,
            boolean isMirror,
            String finalPrompt,
            ImageUploadResponseDTO imageResponse
    ) {
        House house = houseService.createTemplateHouse(user, banner, finalPrompt, floorPlanId, isMirror);

        GenerateImage generateImage = generateImageService.createGenerateImage(
                imageResponse,
                house,
                GenerateImageType.LIST
        );

        creditService.commitCreditDeletion(lockedCredit);
        userService.updateHasGeneratedImage(user);
        return BannerGenerateImageResponse.of(generateImage.getId());
    }

    private FloorPlan getFloorPlanOrThrow(House house) {
        return houseFloorPlanRepository.findHouseFloorPlanByHouseId(house.getId())
                .map(HouseFloorPlan::getFloorPlan)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));
    }
}
