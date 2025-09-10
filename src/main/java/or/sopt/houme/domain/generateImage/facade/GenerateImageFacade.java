package or.sopt.houme.domain.generateImage.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.generateImage.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoListResponse;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.service.AsyncGenerateImageService;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImage.service.GenerateImageTransactionService;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.openai.facade.OpenAiFacade;
import or.sopt.houme.domain.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.taste.dto.response.TagDTO;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.service.TagService;
import or.sopt.houme.domain.taste.service.TasteTagService;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.CreditException;
import or.sopt.houme.global.api.handler.GenerateImageException;
import or.sopt.houme.global.api.handler.ImageFallbackException;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.constant.S3Constant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateImageFacade {

    private final GenerateImageService generateImageService;
    private final OpenAiFacade openAiFacade;
    private final HouseService houseService;
    private final CreditService creditService;
    private final TasteTagService tasteTagService;
    private final UserService userService;
    private final TagService tagService;

    // 비동기 서비스
    private final AsyncGenerateImageService asyncGenerateImageService;

    // 별도의 트랜잭션 분리 클래스
    private final GenerateImageTransactionService generateImageTransactionService;

    // 스프링을 이용한 이미지 생성
    @Transactional
    public ImageInfoResponse generateImage(User user, GenerateImageRequest generateImageRequest){

            /**
             * redis 저장 (user랑 상태값)
             * 재요청시 user 조회 (상태값은 이미지 받았는지 판단)
             * 상태값이 요청 후 받았다( -> 재요청 가능 )
             */

            // 크레딧 감소
            creditService.decreaseCreditAtomically(user);

            Activity activity;
            try {
                    activity = Activity.valueOf(generateImageRequest.activity());
            } catch (IllegalArgumentException e){
                    throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }
            // 주요 활동 업데이트
            House house = houseService.updateHouseActivity(generateImageRequest.houseId(), activity);

            // house_floor_plan 생성 및 저장
            houseService.saveHouseFloorPlan(house, generateImageRequest.floorPlan().floorPlanId());

            // 복층일 경우 침대 제외
            if (!house.getStructure().equals(Structure.DUPLEX)){
                log.info("복층이 아닌 경우 침대 추가");
                generateImageRequest.selectiveIds().add(generateImageRequest.bedId());
            }

            // house furniture 저장
            houseService.saveHouseFurniture(house, generateImageRequest.selectiveIds());

            // 가구 식별자 ID
            PromptFurnitureListDTO promptFurnitureListDTO = PromptFurnitureListDTO.of(generateImageRequest.selectiveIds());

            Equilibrium equilibrium;
            try {
                equilibrium = Equilibrium.valueOf(generateImageRequest.equilibrium());
            } catch (IllegalArgumentException e){
                throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }

            // House와 무드보드들 저장
            houseService.saveHouseTaste(house, generateImageRequest.moodBoardIds());

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

            // house에 프롬프트 저장
            houseService.saveHousePrompt(house, imageUploadResponseDTO.getPullPrompt());

            GenerateImage generateImage;

            try {
                // 도면 이미지 생성
                generateImage = generateImageService.createGenerateImage(imageUploadResponseDTO, house);

            } catch (Exception e){

                // 이미지 재요청 시도하라는 예외 처리
                throw new GeneralException(ErrorCode.RETRY_GET_IMAGE);
            }

            // 이미지 반환 ImageInfoResponse 생성
            ImageInfoResponse imageInfoResponse = ImageInfoResponse.of(generateImage.getId(), generateImage.getUrl(),
                    generateImageRequest.floorPlan().isMirror(),
                    house.getEquilibrium().getDescription(), house.getForm().getDescription(),
                    tag.getTagNameKr(), user.getName());

            // 만약 Fallback 이미지라면, 예외처리
            if (generateImage.getUrl().equals(S3Constant.FALL_BACK_IMAGE)){
                throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, imageInfoResponse);
            }

            // 먼저 예외 처리 하고 업데이트하기
            // 이미지 생성 여부 업데이트
            userService.updateHasGeneratedImage(user);

            return imageInfoResponse;

        } catch (GenerateImageException e) {
          throw e;
        } catch (Exception e){
            log.info("Image 생성 중 오류 발생 {}", e.getMessage());
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        }
    }


    @Transactional
    public ImageInfoResponse generateImageByFastApi(User user, GenerateImageRequest generateImageRequest){

        /**
         * redis 저장 (user랑 상태값)
         * 재요청시 user 조회 (상태값은 이미지 받았는지 판단)
         * 상태값이 요청 후 받았다( -> 재요청 가능 )
         */

        // 크레딧 감소
        creditService.decreaseCreditAtomically(user);

        Activity activity;
        try {
            activity = Activity.valueOf(generateImageRequest.activity());
        } catch (IllegalArgumentException e){
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        // 주요 활동 업데이트
        House house = houseService.updateHouseActivity(generateImageRequest.houseId(), activity);

        // house_floor_plan 생성 및 저장
        houseService.saveHouseFloorPlan(house, generateImageRequest.floorPlan().floorPlanId());

        // 복층일 경우 침대 제외
        if (!house.getStructure().equals(Structure.DUPLEX)){
            log.info("복층이 아닌 경우 침대 추가");
            generateImageRequest.selectiveIds().add(generateImageRequest.bedId());
        }

        // house furniture 저장
        houseService.saveHouseFurniture(house, generateImageRequest.selectiveIds());

        // 가구 식별자 ID
        PromptFurnitureListDTO promptFurnitureListDTO = PromptFurnitureListDTO.of(generateImageRequest.selectiveIds());

        Equilibrium equilibrium;
        try {
            equilibrium = Equilibrium.valueOf(generateImageRequest.equilibrium());
        } catch (IllegalArgumentException e){
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        // House와 무드보드들 저장
        houseService.saveHouseTaste(house, generateImageRequest.moodBoardIds());

        // 최고 순위 찾기
        Tag tag = tasteTagService.getPriorityId(generateImageRequest.moodBoardIds());

        PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(
                generateImageRequest.floorPlan().floorPlanId(),
                tag.getId(),
                equilibrium,
                promptFurnitureListDTO
        );

        try {

            // OpenAI로 image 생성
            ImageUploadResponseDTO imageUploadResponseDTO = openAiFacade.makeImageByFastApi(promptRequestDTO);

            // house에 프롬프트 저장
            houseService.saveHousePrompt(house, imageUploadResponseDTO.getPullPrompt());

            GenerateImage generateImage;

            try {
                // 도면 이미지 생성
                generateImage = generateImageService.createGenerateImage(imageUploadResponseDTO, house);

            } catch (Exception e){

                // 이미지 생성 중 오류가 발생하면 재요청하라는 예외 반환
                throw new GeneralException(ErrorCode.RETRY_GET_IMAGE);
            }

            // 이미지 반환 ImageInfoResponse 생성
            ImageInfoResponse imageInfoResponse = ImageInfoResponse.of(generateImage.getId(), generateImage.getUrl(),
                    generateImageRequest.floorPlan().isMirror(),
                    house.getEquilibrium().getDescription(), house.getForm().getDescription(),
                    tag.getTagNameKr(), user.getName());

            // 만약 Fallback 이미지라면, 예외처리
            if (generateImage.getUrl().equals(S3Constant.FALL_BACK_IMAGE)){
                throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, imageInfoResponse);
            }

            // 이미지 생성 여부 업데이트
            userService.updateHasGeneratedImage(user);

            return imageInfoResponse;
        } catch (GenerateImageException e) {
            throw e;
        } catch (Exception e){
            log.info("Image 생성 중 오류 발생 {}", e.getMessage());
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        }
    }

    // 비동기 이미지 생성 요청
    public ImageInfoListResponse generateImageBy2ea(User user, GenerateImageRequest generateImageRequest) {

        // finally 블록에서 사용하기 위해 선언
        Credit lockedCredit = null;

        try {
            // 크레딧 락 획득 및 상태 변경 (짧은 트랜잭션)
            lockedCredit = creditService.tryLockAndGetCredit(user);

            Activity activity;
            try {
                // 활동범위에 값이 유효하지 않으면 예외처리
                activity = Activity.valueOf(generateImageRequest.activity());
            } catch (IllegalArgumentException e) {
                log.error("활동범위가 유효하지 않음: {}", generateImageRequest.activity());
                throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }

            // 기존 house에 주요활동 업데이트하기 (저장)
            House house = houseService.updateHouseActivity(generateImageRequest.houseId(), activity);

            // house_floor_plan 생성 및 저장
            houseService.saveHouseFloorPlan(house, generateImageRequest.floorPlan().floorPlanId());

            // 복층일 경우 침대 제외
            if (!house.getStructure().equals(Structure.DUPLEX)) {
                log.info("복층이 아닌 경우 침대 추가");
                generateImageRequest.selectiveIds().add(generateImageRequest.bedId());
            }

            // house furniture 저장
            houseService.saveHouseFurniture(house, generateImageRequest.selectiveIds());

            // 가구 식별자 ID
            PromptFurnitureListDTO promptFurnitureListDTO = PromptFurnitureListDTO.of(generateImageRequest.selectiveIds());

            Equilibrium equilibrium;
            try {
                // 평형범위에 값이 유효하지 않으면 예외처리
                equilibrium = Equilibrium.valueOf(generateImageRequest.equilibrium());
            } catch (IllegalArgumentException e) {
                log.error("평형범위가 유효하지 않음: {}", generateImageRequest.equilibrium());
                throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }

            // House와 무드보드들 저장
            houseService.saveHouseTaste(house, generateImageRequest.moodBoardIds());

            // 최고 순위 2개 찾기
            List<TagDTO> priorityIdList = tasteTagService.getPriorityIdList(generateImageRequest.moodBoardIds());

            // 비동기 이미지 생성 리스트 (태그가 2개가 아닐 경우가 있기 떄문에 대비함)
            List<CompletableFuture<ImageUploadResponseDTO>> futures = new ArrayList<>();

            // 이미지 생성 태그 1번 준비
            PromptRequestDTO promptRequestDTO1 = PromptRequestDTO.of(
                    generateImageRequest.floorPlan().floorPlanId(),
                    priorityIdList.get(0).id(),
                    equilibrium,
                    promptFurnitureListDTO
            );

            // OpenAI로 image 비동기 생성
            // 1번 이미지 (항상 실행됨)
            futures.add(asyncGenerateImageService.generateImageAsync(promptRequestDTO1));

            // 2번째 태그가 존재할 시에 2번 이미지 준비
            if (priorityIdList.size() > 1) {
                // 이미지 생성 태그 2번 준비
                PromptRequestDTO promptRequestDTO2 = PromptRequestDTO.of(
                        generateImageRequest.floorPlan().floorPlanId(),
                        priorityIdList.get(1).id(),
                        equilibrium,
                        promptFurnitureListDTO
                );

                // OpenAI로 image 비동기 생성
                // 2번 이미지
                futures.add(asyncGenerateImageService.generateImageAsync(promptRequestDTO2));
            }

            // allOf로 모든 1번, 2번 이미지 생성 기다리기
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            try {

                // --- 결과 대기 및 Timeout 설정 ---
                // 둘 중에 하나라도 200초가 넘어가면 타임아웃 처리
                allFutures.orTimeout(200, TimeUnit.SECONDS).join();

                // 모든 비동기 작업이 성공했을 때만 DB에 결과를 저장
                List<ImageUploadResponseDTO> results = futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());

                // 리스트가 비어있다면, 재요청 시도하라는 반환 (429, Too_Many_Requests)
                if (results.isEmpty()){
                    throw new GeneralException(ErrorCode.RETRY_GET_IMAGE);
                }

                // fallback 이미지 저장
                List<ImageInfoResponse> fallbackResponses = new ArrayList<>();

                // 만들어진 이미지가 Fallback 이미지라면, 예외처리
                for (int i = 0; i < results.size(); i++) {
                    if (results.get(i).getImageLink().equals(S3Constant.FALL_BACK_IMAGE)){
                        fallbackResponses.add(
                                ImageInfoResponse.of(null, results.get(i).getImageLink(),
                                        generateImageRequest.floorPlan().isMirror(),
                                        generateImageRequest.equilibrium(),
                                        house.getForm().getDescription(),
                                        priorityIdList.get(i).tagNameKr(),
                                        user.getName()
                                )
                        );
                    }
                }
                // fallback 이미지가 포함되어 있다면 예외처리
                if (!fallbackResponses.isEmpty()) {
                    throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, fallbackResponses);
                }

                // DB 작업을 별도의 트랜잭션 클래스의 메서드로 분리하여 호출 (크레딧 차감은 여기서)
                List<ImageInfoResponse> imageInfoResponses = generateImageTransactionService.saveResultsAndCreateResponse(
                                user,
                                house,
                                results,
                                generateImageRequest,
                                priorityIdList,
                                lockedCredit
                        );


                // DTO로 변환
                ImageInfoListResponse imageInfoListResponse = ImageInfoListResponse.of(imageInfoResponses);

                return imageInfoListResponse;

            } catch (CompletionException | CancellationException e) {
                // CancellationException도 함께 처리 (이미 취소됐다는 예외)

                // 아직 완료되지 않은 다른 작업들을 강제로 취소
                futures.forEach(future -> future.cancel(true));

                // 예외 원인 확인 (CompletionException으로 감싸진 TimeoutException 인지)
                Throwable cause = e.getCause();

                if (cause instanceof TimeoutException) {
                    // 원인이 TimeoutException일 경우
                    log.error("이미지 생성 작업 시간 초과: {}", cause.getMessage());
                    throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_TIMEOUT);
                } else {
                    // 그 외 다른 예외일 경우 (AI API 오류 등)
                    log.error("비동기 이미지 생성 작업 중 오류 발생: {}", cause != null ? cause.getMessage() : e.getMessage());
                    throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
                }
            }
        } catch (GenerateImageException | ImageFallbackException | CreditException e){
            // 이미지 생성 중 어떤 예외라도 발생하면 크레딧 상태 복구
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING){
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw e;
        } catch (Exception e){
            log.error("이미지 생성 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            // 예상치 못한 예외 발생 시에도 크레딧 상태 복구
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING){
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        } finally {
            // 어떤 경우든 락 최종 해제
            if (lockedCredit != null){
                creditService.releaseLock(user);
            }
        }
    }

    // houseId로 결과 이미지 찾아오기
    public ImageInfoResponse getFallBackImage(User user, Long houseId){
        House houseById = houseService.findHouseById(houseId);

        GenerateImage generateImage;

        try {
            generateImage = generateImageService.findGenerateImageByHouseId(houseId);
        } catch (Exception e) {

            // 다시 시도하라는 예외처리
            throw new GeneralException(ErrorCode.RETRY_GET_IMAGE);
        }

        // 반전여부
        boolean isMirror = houseService.getIsMirrorByHouseId(houseId);
        // 평형
        String equilibrium = houseById.getEquilibrium().getDescription();
        // 집 형태
        String houseForm = houseById.getForm().getDescription();

        // 태그 찾기
        Tag tag = tagService.findTagByUserIdAndImageId(user.getId(), generateImage.getId());

        return ImageInfoResponse.of(generateImage.getId(), generateImage.getUrl(), isMirror, equilibrium, houseForm, tag.getTagNameKr(), user.getName());
    }
}
