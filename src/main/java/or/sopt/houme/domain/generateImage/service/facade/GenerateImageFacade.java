package or.sopt.houme.domain.generateImage.service.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.model.entity.CreditStatus;
import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.generateImage.presentation.dto.SelectedTagInfo;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoListResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.SelectionStrategy;
import or.sopt.houme.domain.generateImage.service.AsyncGenerateImageService;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImage.service.GenerateImageTransactionService;
import or.sopt.houme.domain.generateImage.service.imageGenerationLog.ImageGenerationTransactionService;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.service.GeminiImageService;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.generateImage.service.openai.facade.OpenAiFacade;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.generateImage.service.prompt.PromptService;
import or.sopt.houme.domain.house.presentation.taste.dto.response.TagDTO;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.model.taste.entity.Taste;
import or.sopt.houme.domain.house.service.taste.TagService;
import or.sopt.houme.domain.house.service.taste.TasteService;
import or.sopt.houme.domain.house.service.taste.TasteTagService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.*;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.constant.S3Constant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateImageFacade {

    private final GenerateImageService generateImageService;
    private final OpenAiFacade openAiFacade;
    private final PromptService promptService;
    private final GeminiImageService geminiImageService;
    private final HouseService houseService;
    private final CreditService creditService;
    private final TasteTagService tasteTagService;
    private final UserService userService;
    private final TagService tagService;
    private final FurnitureService furnitureService;

    // 비동기 서비스
    private final AsyncGenerateImageService asyncGenerateImageService;

    // 별도의 트랜잭션 분리 클래스
    private final GenerateImageTransactionService generateImageTransactionService;

    // 무드보드 서비스
    private final TasteService tasteService;

    // A/B 로그 저장 서비스
    private final ImageGenerationTransactionService imageGenerationTransactionService;

    // 스프링을 이용한 이미지 생성
    @Transactional
    public ImageInfoResponse generateImage(User user, GenerateImageRequest generateImageRequest) {
        return generateImageInternal(user, generateImageRequest, false);
    }

    @Transactional
    public ImageInfoResponse generateImageByGemini(User user, GenerateImageRequest generateImageRequest) {
        return generateImageInternal(user, generateImageRequest, true);
    }

    private ImageInfoResponse generateImageInternal(User user, GenerateImageRequest generateImageRequest, boolean useGemini) {

        /**
         * redis 저장 (user랑 상태값)
         * 재요청시 user 조회 (상태값은 이미지 받았는지 판단)
         * 상태값이 요청 후 받았다( -> 재요청 가능 )
         */

        // 크레딧 감소
        creditService.decreaseCreditAtomically(user);

        // Enum 타입의 유효성 검증
        Activity activity = enumValueOf(Activity.class, generateImageRequest.activity());
        Equilibrium equilibrium = enumValueOf(Equilibrium.class, generateImageRequest.equilibrium());

        // 주요 활동 업데이트
        House house = houseService.updateHouseActivity(generateImageRequest.houseId(), activity);

        // house_floor_plan 생성 및 저장
        houseService.saveHouseFloorPlan(house, generateImageRequest.floorPlan().floorPlanId(), generateImageRequest.floorPlan().isMirror());

        // 침대 ID 찾기
        Optional<Long> bedId = furnitureService.findBedId(generateImageRequest.selectiveIds());

        // 복층이 아닌 경우 침대 추가
        if (!house.getStructure().equals(Structure.DUPLEX) && bedId.isPresent()) {
            log.info("복층이 아닌 경우 침대 추가");
            generateImageRequest.selectiveIds().add(bedId.get());
        }

        // house furniture 저장
        houseService.saveHouseFurniture(house, generateImageRequest.selectiveIds());

        // 가구 식별자 ID
        PromptFurnitureListDTO promptFurnitureListDTO = PromptFurnitureListDTO.of(generateImageRequest.selectiveIds());

        // House와 무드보드들 저장
        houseService.saveHouseTaste(house, generateImageRequest.moodBoardIds());

        // 가장 우선순위가 높은 무드보드 id 제공
        Tag tag = tasteTagService.getPriorityId(generateImageRequest.moodBoardIds());

        PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(generateImageRequest.floorPlan().floorPlanId(),
                tag.getId(), equilibrium, promptFurnitureListDTO);

        try {

            // OpenAI/Gemini로 image 생성
            ImageUploadResponseDTO imageUploadResponseDTO;

            if (useGemini) {
                String prompt = promptService.makePrompt(promptRequestDTO);
                imageUploadResponseDTO = geminiImageService.createImage(prompt);
            } else {
                imageUploadResponseDTO = openAiFacade.makeImage(promptRequestDTO);
            }

            // house에 프롬프트 저장
            houseService.saveHousePrompt(house, imageUploadResponseDTO.getPullPrompt());

            GenerateImage generateImage;

            try {
                // 도면 이미지 생성
                generateImage = generateImageService.createGenerateImage(
                        imageUploadResponseDTO,
                        house,
                        GenerateImageType.REGULAR,
                        null
                );

            } catch (Exception e) {

                // 이미지 재요청 시도하라는 예외 처리
                throw new GeneralException(ErrorCode.RETRY_GET_IMAGE);
            }

            // 이미지 반환 ImageInfoResponse 생성
            ImageInfoResponse imageInfoResponse = ImageInfoResponse.of(generateImage.getId(), generateImage.getUrl(),
                    generateImageRequest.floorPlan().isMirror(), house.getEquilibrium().getDescription(),
                    house.getForm().getDescription(), tag.getTagNameKr(), user.getName());

            // 만약 Fallback 이미지라면, 예외처리
            if (generateImage.getUrl().equals(S3Constant.FALL_BACK_IMAGE)) {
                log.error("폴백 이미지가 생성되었습니다.");
                throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, imageInfoResponse);
            }

            // 먼저 예외 처리 하고 업데이트하기
            // 이미지 생성 여부 업데이트
            userService.updateHasGeneratedImage(user);

            return imageInfoResponse;

        } catch (ValidException validException) {
            // 유효값 검증 실패시
            log.error("유효값 검증 실패: {}", validException.getMessage(), validException);
            throw new ValidException(ErrorCode.NOT_VALID_EXCEPTION);
        } catch (GenerateImageException e) {
            throw e;
        } catch (Exception e) {
            log.info("Image 생성 중 오류 발생 {}", e.getMessage());
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        }
    
    }

    public ImageInfoResponse generateImageByFastApi(User user, GenerateImageRequest generateImageRequest) {
        return generateImageByFastApiInternal(user, generateImageRequest, false);
    }

    public ImageInfoResponse generateImageByFastApiGemini(User user, GenerateImageRequest generateImageRequest) {
        return generateImageByFastApiInternal(user, generateImageRequest, true);
    }

    private ImageInfoResponse generateImageByFastApiInternal(User user, GenerateImageRequest generateImageRequest, boolean useGemini) {

        /**
         * [짧은 트랜잭션이 일어나는 부분] (하나의 로직으로 처리)
         * - house 주요 활동 업데이트
         * - house furniture 저장
         * - house 무드보드들 저장
         * - house prompt 저장
         * - 이미지 생성 여부 업데이트
         * - 이미지 저장
         * - 크레딧 차감 확정
         * =====================
         * [본 로직에서 일어나는 부분]
         * 로그 처리 (이것도 하나의 로직 => 트랜잭션 처리됨)
         * 크레딧 락 획득 및 상태 변경
         */

        // 이미 생성된 이미지가 존재하는 houseId면 fall api로 요청하라고 넘기기
        try{
            GenerateImage generateImageByHouseId = generateImageService.findGenerateImageByHouseId(generateImageRequest.houseId());
            if (generateImageByHouseId != null) {
                log.info("houseId: {}로 생성된 이미지 존재함", generateImageRequest.houseId());
                // 이미지 생성 중 오류가 발생하면 재요청하라는 예외 반환
                throw new GeneralException(ErrorCode.RETRY_GET_IMAGE);
            }
        } catch (GenerateImageException e) {
            // 이미지 생성 진행
            log.info("houseId: {}로 생성된 이미지 없음", generateImageRequest.houseId());
        }

        Credit lockedCredit = null;
        // 크레딧 감소
        try {

            // 크레딧 락 획득 및 상태 변경 (짧은 트랜잭션)
            lockedCredit = creditService.tryLockAndGetCredit(user);

            // Enum 타입의 유효성 검증
            Activity activity = enumValueOf(Activity.class, generateImageRequest.activity());
            Equilibrium equilibrium = enumValueOf(Equilibrium.class, generateImageRequest.equilibrium());

            // 가구 식별자 ID
            PromptFurnitureListDTO promptFurnitureListDTO = PromptFurnitureListDTO.of(generateImageRequest.selectiveIds());

            // 최고 순위 찾기
            Tag priorityTag = tasteTagService.getPriorityId(generateImageRequest.moodBoardIds());

            PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(generateImageRequest.floorPlan().floorPlanId(),
                    priorityTag.getId(), equilibrium, promptFurnitureListDTO);

            // OpenAI/Gemini로 image 생성
            ImageUploadResponseDTO imageUploadResponseDTO;

            if (useGemini) {
                String prompt = promptService.makePrompt(promptRequestDTO);
                imageUploadResponseDTO = geminiImageService.createImage(prompt);
            } else {
                imageUploadResponseDTO = openAiFacade.makeImageByFastApi(promptRequestDTO);
            }

            ImageInfoResponse imageInfoResponse = generateImageTransactionService.saveAllDataAndConfirmCredit(
                    user,
                    lockedCredit,
                    generateImageRequest,
                    imageUploadResponseDTO,
                    priorityTag,
                    activity,
                    GenerateImageType.REGULAR
            );

            // 만약 Fallback 이미지라면, 예외처리
            if (imageInfoResponse.imageUrl().equals(S3Constant.FALL_BACK_IMAGE)) {
                log.error("폴백 이미지가 생성되었습니다.");
                throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, imageInfoResponse);
            }

            /*
             * 사용자 로그 저장 사용자, 무드보드 객체들, 이미지, 스타일 태그 객체들
             * */
            String type = "B";
            saveLog(user.getId(), type, generateImageRequest.moodBoardIds(), List.of(imageInfoResponse));

            return imageInfoResponse;
        } catch (ValidException validException) {
            // 유효값 검증 실패시
            log.error("유효값 검증 실패: {}", validException.getMessage(), validException);
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new ValidException(ErrorCode.NOT_VALID_EXCEPTION);
        } catch (GenerateImageException | ImageFallbackException | CreditException e) {
            // 이미지 생성 중 어떤 예외라도 발생하면 크레딧 상태 복구
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw e;
        } catch (Exception e) {
            log.info("Image 생성 중 오류 발생 {}", e.getMessage());
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        } finally {
            // 어떤 경우든 락 최종 해제
            if (lockedCredit != null) {
                creditService.releaseLock(user);
            }
        }
    
    }

    // 비동기 이미지 생성 요청
    public ImageInfoListResponse generateImageBy2ea(User user, GenerateImageRequest generateImageRequest) {
        return generateImageBy2eaInternal(user, generateImageRequest, false);
    }

    public ImageInfoListResponse generateImageBy2eaGemini(User user, GenerateImageRequest generateImageRequest) {
        return generateImageBy2eaInternal(user, generateImageRequest, true);
    }

    private ImageInfoListResponse generateImageBy2eaInternal(User user, GenerateImageRequest generateImageRequest, boolean useGemini) {

        // finally 블록에서 사용하기 위해 선언
        Credit lockedCredit = null;

        try {
            // 크레딧 락 획득 및 상태 변경 (짧은 트랜잭션)
            lockedCredit = creditService.tryLockAndGetCredit(user);

            // Enum 타입의 유효성 검증
            Activity activity = enumValueOf(Activity.class, generateImageRequest.activity());
            Equilibrium equilibrium = enumValueOf(Equilibrium.class, generateImageRequest.equilibrium());

            // 기존 house에 주요활동 업데이트하기 (저장)
            House house = houseService.updateHouseActivity(generateImageRequest.houseId(), activity);

            // house_floor_plan 생성 및 저장
            houseService.saveHouseFloorPlan(house, generateImageRequest.floorPlan().floorPlanId(), generateImageRequest.floorPlan().isMirror());

            // 침대 ID 찾기
            Optional<Long> bedId = furnitureService.findBedId(generateImageRequest.selectiveIds());

            // 복층이 아닌 경우 침대 추가
            if (!house.getStructure().equals(Structure.DUPLEX) && bedId.isPresent()) {
                log.info("복층이 아닌 경우 침대 추가");
                generateImageRequest.selectiveIds().add(bedId.get());
            }

            // house furniture 저장
            houseService.saveHouseFurniture(house, generateImageRequest.selectiveIds());

            // 가구 식별자 ID
            PromptFurnitureListDTO promptFurnitureListDTO = PromptFurnitureListDTO.of(generateImageRequest.selectiveIds());

            // House와 무드보드들 저장
            houseService.saveHouseTaste(house, generateImageRequest.moodBoardIds());

            // 최고 순위 2개 찾기
            List<TagDTO> priorityIdList = tasteTagService.getPriorityIdList(generateImageRequest.moodBoardIds());

            // 비동기 이미지 생성 리스트 (태그가 2개가 아닐 경우가 있기 떄문에 대비함)
            List<CompletableFuture<ImageUploadResponseDTO>> futures = new ArrayList<>();

            // 이미지 생성 태그 1번 준비
            PromptRequestDTO promptRequestDTO1 = PromptRequestDTO.of(generateImageRequest.floorPlan().floorPlanId(),
                    priorityIdList.get(0).id(), equilibrium, promptFurnitureListDTO);

            // 1번 이미지 (항상 실행됨)
            futures.add(requestAsyncImage(promptRequestDTO1, useGemini));

            // 2번째 태그가 존재할 시에 2번 이미지 준비
            if (priorityIdList.size() > 1) {
                // 이미지 생성 태그 2번 준비
                PromptRequestDTO promptRequestDTO2 = PromptRequestDTO.of(generateImageRequest.floorPlan().floorPlanId(),
                        priorityIdList.get(1).id(), equilibrium, promptFurnitureListDTO);

                // 2번 이미지
                futures.add(requestAsyncImage(promptRequestDTO2, useGemini));
            }

            // allOf로 모든 1번, 2번 이미지 생성 기다리기
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            try {

                // --- 결과 대기 및 Timeout 설정 ---
                // 둘 중에 하나라도 200초가 넘어가면 타임아웃 처리
                allFutures.orTimeout(200, TimeUnit.SECONDS).join();

                // 모든 비동기 작업이 성공했을 때만 DB에 결과를 저장
                List<ImageUploadResponseDTO> results = collectAsyncResults(futures);

                // 리스트가 비어있다면, 재요청 시도하라는 반환 (429, Too_Many_Requests)
                if (results.isEmpty()) {
                    throw new GeneralException(ErrorCode.RETRY_GET_IMAGE);
                }

                // fallback 이미지 저장
                List<ImageInfoResponse> fallbackResponses = new ArrayList<>();

                // 만들어진 이미지가 Fallback 이미지라면, 예외처리
                for (int i = 0; i < results.size(); i++) {
                    if (results.get(i).getImageLink().equals(S3Constant.FALL_BACK_IMAGE)) {
                        fallbackResponses.add(ImageInfoResponse.of(null, results.get(i).getImageLink(),
                                generateImageRequest.floorPlan().isMirror(), generateImageRequest.equilibrium(),
                                house.getForm().getDescription(), priorityIdList.get(i).tagNameKr(), user.getName()));
                    }
                }
                // fallback 이미지가 포함되어 있다면 예외처리
                if (!fallbackResponses.isEmpty()) {
                    log.error("폴백 이미지가 생성되었습니다.");
                    throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, fallbackResponses);
                }

                // DB 작업을 별도의 트랜잭션 클래스의 메서드로 분리하여 호출 (크레딧 차감은 여기서)
                List<ImageInfoResponse> imageInfoResponses = generateImageTransactionService.saveResultsAndCreateResponse(
                        user,
                        house,
                        results,
                        generateImageRequest,
                        priorityIdList,
                        lockedCredit,
                        GenerateImageType.REGULAR
                );


                // DTO로 변환
                ImageInfoListResponse imageInfoListResponse = ImageInfoListResponse.of(imageInfoResponses);

                /*
                 * 사용자 로그 저장 사용자, 무드보드 객체들, 이미지, 스타일 태그 객체들
                 * */
                String type = "A";
                saveLog(user.getId(), type, generateImageRequest.moodBoardIds(), imageInfoResponses);

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
        } catch (ValidException validException) {
            // 유효값 검증 실패시
            log.error("유효값 검증 실패: {}", validException.getMessage(), validException);
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new ValidException(ErrorCode.NOT_VALID_EXCEPTION);
        } catch (GenerateImageException | ImageFallbackException | CreditException e) {
            // 이미지 생성 중 어떤 예외라도 발생하면 크레딧 상태 복구
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw e;
        } catch (Exception e) {
            log.error("이미지 생성 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            // 예상치 못한 예외 발생 시에도 크레딧 상태 복구
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        } finally {
            // 어떤 경우든 락 최종 해제
            if (lockedCredit != null) {
                creditService.releaseLock(user);
            }
        }
    
    }

    private CompletableFuture<ImageUploadResponseDTO> requestAsyncImage(
            PromptRequestDTO promptRequestDTO,
            boolean useGemini
    ) {
        if (useGemini) {
            return asyncGenerateImageService.generateGeminiImageAsync(promptRequestDTO);
        }
        return asyncGenerateImageService.generateImageAsync(promptRequestDTO);
    }

    private List<ImageUploadResponseDTO> collectAsyncResults(
            List<CompletableFuture<ImageUploadResponseDTO>> futures
    ) {
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    // houseId로 결과 이미지 찾아오기
    public ImageInfoResponse getFallBackImage(User user, Long houseId) {
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

        return ImageInfoResponse.of(generateImage.getId(), generateImage.getUrl(), isMirror,
                equilibrium, houseForm, tag.getTagNameKr(), user.getName());
    }

    // A/B 로그 저장 내부 메서드 (트랜잭션 하나로 처리)
    private void saveLog(Long userId, String type, List<Long> moodBoardIds, List<ImageInfoResponse> imageInfoResponses) {

        List<Taste> tasteList = tasteService.getTasteList(moodBoardIds);

        // 태그 빈도 계산
        List<Tag> choiceTagList = moodBoardIds.stream().map(tagService::findTagByTasteId).toList();

        List<SelectedTagInfo> selectedTagInfoList = getTagInfoList(choiceTagList);

        // 로그 저장
        List<Tag> distinctTagsByTasteIds = tasteTagService.findDistinctTagsByTasteIds(moodBoardIds);

        imageGenerationTransactionService.saveImageGenerationLog(userId, type, imageInfoResponses.size(),
                tasteList, distinctTagsByTasteIds, imageInfoResponses, selectedTagInfoList);
    }

    // Tag 순위 선정 방식이 담긴 리스트 받기
    private List<SelectedTagInfo> getTagInfoList(List<Tag> choiceTagList) {
        Map<Tag, Long> tagCountMap = choiceTagList.stream()
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        List<SelectedTagInfo> result = new ArrayList<>();

        // 1위 선정
        List<Map.Entry<Tag, Long>> sortedList = tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> {
            int compare = Long.compare(e2.getValue(), e1.getValue());
            if (compare == 0) {
                return Integer.compare(e1.getKey().getPriority(), e2.getKey().getPriority());
            }
            return compare;
        }).toList();

        // 만약 비어있으면 null 처리 (실제로는 없음, 무드보드 선택이 안됐다는 뜻)
        if (sortedList.isEmpty()) throw new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY);

        // 가장 우선시 되는 태그 꺼내기
        Tag topTag = sortedList.get(0).getKey();
        // 가장 우선시 되는 빈도 수 확인하기
        long topCount = sortedList.get(0).getValue();

        // 우선순위 리스트가 1보다 크고, 2순위에 있는 태그의 빈도가 1순위와 같은지
        boolean topTiedByPriority = sortedList.size() > 1 && sortedList.get(1).getValue().equals(topCount);

        // 위에 해당하면, 2개 중 우선순위를 통해 고름
        // 아니라면, 가장 많은 태그를 고름
        String topReason = topTiedByPriority ? SelectionStrategy.TOP2_BY_PRIORITY.getStrategy() : SelectionStrategy.TOP1.getStrategy();

        result.add(new SelectedTagInfo(topTag, topReason));

        // 1위 제외 후 2위 선정
        Map<Tag, Long> remaining = new HashMap<>(tagCountMap);
        // 1위에 있던 태그 제거
        remaining.remove(topTag);

        // 제거 후 안 비어있으면
        if (!remaining.isEmpty()) {
            // 또 다시 1순위 선정
            List<Map.Entry<Tag, Long>> secondSorted = remaining.entrySet().stream()
                    .sorted((e1, e2) -> {
                int compare = Long.compare(e2.getValue(), e1.getValue());
                if (compare == 0) {
                    return Integer.compare(e1.getKey().getPriority(), e2.getKey().getPriority());
                }
                return compare;
            }).toList();

            // 가장 우선시 되는 태그 꺼내기
            Tag secondTag = secondSorted.get(0).getKey();
            // 가장 우선시 되는 빈도 수 확인하기
            long secondCount = secondSorted.get(0).getValue();

            // 우선순위 리스트가 1보다 크고, 2순위에 있는 태그의 빈도가 1순위와 같은지
            boolean secondTiedByPriority = secondSorted.size() > 1 && secondSorted.get(1).getValue().equals(secondCount);

            // 위에 해당하면, 2개 중 우선순위를 통해 고름
            // 아니라면, 가장 많은 태그를 고름
            String secondReason = secondTiedByPriority ? SelectionStrategy.TOP2_BY_PRIORITY.getStrategy() : SelectionStrategy.TOP1.getStrategy();

            result.add(new SelectedTagInfo(secondTag, secondReason));
        }

        // 반환
        return result;
    }

    // Enum 타입의 유효성 검증 로직
    private <E extends Enum<E>> E enumValueOf(Class<E> enumType, String value) {
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            log.warn("유효성 검증 실패 {}: {}", enumType.getSimpleName(), value);
            throw new ValidException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }

}
