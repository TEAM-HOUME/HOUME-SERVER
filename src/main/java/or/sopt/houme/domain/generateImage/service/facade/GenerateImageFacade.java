package or.sopt.houme.domain.generateImage.service.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.model.entity.CreditStatus;
import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.ActivityFurniture;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.repository.ActivityFurnitureRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.generateImage.presentation.dto.SelectedTagInfo;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.presentation.dto.request.BannerGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageV4Request;
import or.sopt.houme.domain.generateImage.presentation.dto.request.OtherStyleGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.ProductGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.response.BannerGenerateImageResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.GenerateImageV4Response;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoListResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.OtherStyleGenerateImageResponse;
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
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.repository.HouseFloorPlanRepository;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
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
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
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
    private static final TypeReference<List<BannerStyleAnswerChip>> STYLE_ANSWER_CHIP_TYPE = new TypeReference<>() {};

    private final GenerateImageService generateImageService;
    private final OpenAiFacade openAiFacade;
    private final PromptService promptService;
    private final GeminiImageService geminiImageService;
    private final BannerRepository bannerRepository;
    private final FloorPlanRepository floorPlanRepository;
    private final HouseService houseService;
    private final HouseFloorPlanRepository houseFloorPlanRepository;
    private final CreditService creditService;
    private final TasteTagService tasteTagService;
    private final UserService userService;
    private final TagService tagService;
    private final FurnitureService furnitureService;
    private final FurnitureTagRepository furnitureTagRepository;
    private final ActivityFurnitureRepository activityFurnitureRepository;
    private final CurationRawProductRepository curationRawProductRepository;
    private final FloorPlanImageJsonCodec floorPlanImageJsonCodec;
    private final ObjectMapper objectMapper;

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
        FloorPlan houseFloorPlan = requireHouseFloorPlan(house);

        // 침대 ID 찾기
        Optional<Long> bedId = furnitureService.findBedId(generateImageRequest.selectiveIds());

        // 복층이 아닌 경우 침대 추가
        if (!houseFloorPlan.getStructure().equals(Structure.DUPLEX) && bedId.isPresent()) {
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
                        GenerateImageType.FULL_FUNNEL
                );

            } catch (Exception e) {

                // 이미지 재요청 시도하라는 예외 처리
                throw new GeneralException(ErrorCode.RETRY_GET_IMAGE);
            }

            // 이미지 반환 ImageInfoResponse 생성
            ImageInfoResponse imageInfoResponse = ImageInfoResponse.of(generateImage.getId(), generateImage.getUrl(),
                    generateImageRequest.floorPlan().isMirror(), houseFloorPlan.getEquilibrium().getDescription(),
                    houseFloorPlan.getForm().getDescription(), tag.getTagNameKr(), user.getName());

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
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_REQUEST);
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
                    GenerateImageType.FULL_FUNNEL
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
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_REQUEST);
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

    public BannerGenerateImageResponse generateBannerImageByGemini(User user, BannerGenerateImageRequest request) {
        Credit lockedCredit = null;

        try {
            log.info("배너 템플릿 기반 인테리어 이미지 생성 시작 userId={}, bannerId={}, answerId={}, floorPlanId={}, view={}, isMirror={}",
                    user.getId(), request.bannerId(), request.answerId(), request.floorPlanId(), request.floorPlanView(), request.isMirror());
            lockedCredit = creditService.tryLockAndGetCredit(user);

            Banner banner = bannerRepository.findByIdWithRawProducts(request.bannerId(), BannerType.BANNER, false)
                    .orElseThrow(() -> new BannerException(ErrorCode.NOT_FOUND_BANNER));
            FloorPlan floorPlan = floorPlanRepository.findById(request.floorPlanId())
                    .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

            BannerStyleAnswerChip selectedChip = parseStyleAnswerChips(banner.getStyleAnswerChipsJson()).stream()
                    .filter(chip -> chip.id() != null && chip.id().equals(request.answerId()))
                    .findFirst()
                    .orElseThrow(() -> new GenerateImageException(ErrorCode.INVALID_BANNER_ANSWER_CHIP));

            String floorPlanImageUrl = resolveFloorPlanImageUrl(floorPlan, request.floorPlanView());
            List<String> referenceImageUrls = buildReferenceImageUrls(banner, selectedChip, floorPlanImageUrl);
            String prompt = buildBannerPrompt(banner, selectedChip, floorPlan);
            log.info(
                    "배너 템플릿 이미지 생성 프롬프트/참고이미지 bannerId={}, answerId={}, prompt={}, referenceImageUrls={}",
                    banner.getId(),
                    selectedChip.id(),
                    prompt,
                    referenceImageUrls
            );
            log.info("AI 호출 준비 완료 bannerId={}, answerId={}, referenceImageCount={}", banner.getId(), selectedChip.id(), referenceImageUrls.size());

            ImageUploadResponseDTO imageUploadResponseDTO =
                    geminiImageService.createImageWithReferences(prompt, referenceImageUrls);
            log.info("AI 호출 완료 bannerId={}, generatedUrl={}", banner.getId(), imageUploadResponseDTO.getImageLink());

            if (imageUploadResponseDTO.getImageLink().equals(S3Constant.FALL_BACK_IMAGE)) {
                log.error("배너 템플릿 기반 인테리어 이미지 생성 중 폴백 이미지가 생성되었습니다. bannerId={}", banner.getId());
                throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, null);
            }

            BannerGenerateImageResponse response = generateImageTransactionService.saveBannerImageAndConfirmCredit(
                    user,
                    lockedCredit,
                    banner,
                    request.floorPlanId(),
                    request.isMirror(),
                    prompt,
                    imageUploadResponseDTO
            );
            log.info("배너 템플릿 기반 인테리어 이미지 생성 저장 완료 imageId={}", response.imageId());
            return response;
        } catch (ValidException validException) {
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw validException;
        } catch (GeneralException e) {
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw e;
        } catch (Exception e) {
            log.error("배너 템플릿 기반 인테리어 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        } finally {
            if (lockedCredit != null) {
                creditService.releaseLock(user);
            }
        }
    }

    public OtherStyleGenerateImageResponse generateOtherStyleImageByGemini(User user, OtherStyleGenerateImageRequest request) {
        Credit lockedCredit = null;

        try {
            log.info("스타일 템플릿 기반 인테리어 이미지 생성 시작 userId={}, bannerId={}, floorPlanId={}, view={}, isMirror={}",
                    user.getId(), request.bannerId(), request.floorPlanId(), request.floorPlanView(), request.isMirror());
            lockedCredit = creditService.tryLockAndGetCredit(user);

            Banner style = bannerRepository.findByIdWithRawProducts(request.bannerId(), BannerType.STYLE, false)
                    .orElseThrow(() -> new BannerException(ErrorCode.NOT_FOUND_STYLE));
            FloorPlan floorPlan = floorPlanRepository.findById(request.floorPlanId())
                    .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

            String floorPlanImageUrl = resolveFloorPlanImageUrl(floorPlan, request.floorPlanView());
            List<String> referenceImageUrls = buildStyleReferenceImageUrls(style, floorPlanImageUrl);
            String prompt = buildStylePrompt(style, floorPlan);
            log.info(
                    "스타일 템플릿 이미지 생성 프롬프트/참고이미지 bannerId={}, prompt={}, referenceImageUrls={}",
                    style.getId(),
                    prompt,
                    referenceImageUrls
            );
            log.info("AI 호출 준비 완료 bannerId={}, referenceImageCount={}", style.getId(), referenceImageUrls.size());

            ImageUploadResponseDTO imageUploadResponseDTO =
                    geminiImageService.createImageWithReferences(prompt, referenceImageUrls);
            log.info("AI 호출 완료 bannerId={}, generatedUrl={}", style.getId(), imageUploadResponseDTO.getImageLink());

            if (imageUploadResponseDTO.getImageLink().equals(S3Constant.FALL_BACK_IMAGE)) {
                log.error("스타일 템플릿 기반 인테리어 이미지 생성 중 폴백 이미지가 생성되었습니다. bannerId={}", style.getId());
                throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, null);
            }

            BannerGenerateImageResponse response = generateImageTransactionService.saveBannerImageAndConfirmCredit(
                    user,
                    lockedCredit,
                    style,
                    request.floorPlanId(),
                    request.isMirror(),
                    prompt,
                    imageUploadResponseDTO
            );
            log.info("스타일 템플릿 기반 인테리어 이미지 생성 저장 완료 imageId={}", response.imageId());
            return OtherStyleGenerateImageResponse.of(response.imageId(), response.imageUrl(), response.isMirror());
        } catch (ValidException validException) {
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw validException;
        } catch (GeneralException e) {
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw e;
        } catch (Exception e) {
            log.error("스타일 템플릿 기반 인테리어 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        } finally {
            if (lockedCredit != null) {
                creditService.releaseLock(user);
            }
        }
    }

    public GenerateImageV4Response generateImageV4ByGemini(User user, GenerateImageV4Request request) {
        Credit lockedCredit = null;

        try {
            log.info(
                    "V4 이미지 생성 시작 userId={}, floorPlanId={}, view={}, isMirror={}, moodBoardCount={}, furnitureCount={}",
                    user.getId(),
                    request.floorPlanId(),
                    request.floorPlanView(),
                    request.isMirror(),
                    request.moodBoardIds().size(),
                    request.furnitureIds().size()
            );

            lockedCredit = creditService.tryLockAndGetCredit(user);

            Activity activity = enumValueOf(Activity.class, request.activity());
            Tag selectedTag = tasteTagService.getPriorityId(request.moodBoardIds());
            FloorPlan floorPlan = floorPlanRepository.findById(request.floorPlanId())
                    .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

            List<Long> combinedFurnitureIds = buildCombinedFurnitureIds(activity, request.furnitureIds());
            List<FurnitureTag> matchedFurnitureTags = furnitureTagRepository.findAllByFurnitureIdInAndTagId(
                    combinedFurnitureIds,
                    selectedTag.getId()
            );
            log.info(
                    "V4 이미지 생성에 사용된 furniture_tag ids: {} (tagId={}, furnitureIds={})",
                    matchedFurnitureTags.stream().map(FurnitureTag::getId).toList(),
                    selectedTag.getId(),
                    combinedFurnitureIds
            );

            String floorPlanImageUrl = resolveFloorPlanImageUrlStrict(floorPlan, request.floorPlanView());
            List<String> referenceImageUrls = buildV4ReferenceImageUrls(floorPlanImageUrl, matchedFurnitureTags);
            String prompt = buildV4Prompt(floorPlan, selectedTag, matchedFurnitureTags);

            log.info(
                    "V4 이미지 생성 프롬프트/참고이미지 tagId={}, prompt={}, referenceImageCount={}",
                    selectedTag.getId(),
                    prompt,
                    referenceImageUrls.size()
            );

            ImageUploadResponseDTO imageUploadResponseDTO =
                    geminiImageService.createImageWithReferences(prompt, referenceImageUrls);

            if (imageUploadResponseDTO.getImageLink().equals(S3Constant.FALL_BACK_IMAGE)) {
                log.error("V4 이미지 생성 중 폴백 이미지가 생성되었습니다.");
                throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, null);
            }

            return generateImageTransactionService.saveV4ImageAndConfirmCredit(
                    user,
                    lockedCredit,
                    request.floorPlanId(),
                    request.isMirror(),
                    prompt,
                    imageUploadResponseDTO,
                    activity,
                    combinedFurnitureIds,
                    request.moodBoardIds()
            );
        } catch (ValidException validException) {
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw validException;
        } catch (GeneralException e) {
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw e;
        } catch (Exception e) {
            log.error("V4 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        } finally {
            if (lockedCredit != null) {
                creditService.releaseLock(user);
            }
        }
    }

    public GenerateImageV4Response generateImageByProducts(User user, ProductGenerateImageRequest request) {
        Credit lockedCredit = null;

        try {
            lockedCredit = creditService.tryLockAndGetCredit(user);

            FloorPlan floorPlan = floorPlanRepository.findById(request.floorPlanId())
                    .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

            List<Long> productIds = request.productIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (productIds.isEmpty()) {
                throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_REQUEST);
            }

            List<CurationRawProduct> selectedProducts = curationRawProductRepository.findAllById(productIds);
            if (selectedProducts.size() != productIds.size()) {
                throw new FurnitureException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT);
            }

            String floorPlanImageUrl = resolveFloorPlanImageUrlStrict(floorPlan, request.floorPlanView());
            List<String> referenceImageUrls = buildProductReferenceImageUrls(floorPlanImageUrl, selectedProducts);
            String prompt = buildProductBasedPrompt(floorPlan, selectedProducts);

            ImageUploadResponseDTO imageUploadResponseDTO =
                    geminiImageService.createImageWithReferences(prompt, referenceImageUrls);

            if (imageUploadResponseDTO.getImageLink().equals(S3Constant.FALL_BACK_IMAGE)) {
                throw new ImageFallbackException(ErrorCode.GENERATED_IMAGE_EXCEPTION, null);
            }

            return generateImageTransactionService.saveProductImageAndConfirmCredit(
                    user,
                    lockedCredit,
                    request.floorPlanId(),
                    request.isMirror(),
                    prompt,
                    imageUploadResponseDTO,
                    selectedProducts
            );
        } catch (ValidException validException) {
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw validException;
        } catch (GeneralException e) {
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw e;
        } catch (Exception e) {
            log.error("선택 상품 기반 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
            if (lockedCredit != null && lockedCredit.getStatus() == CreditStatus.PENDING) {
                creditService.rollbackCreditPending(lockedCredit);
            }
            throw new GenerateImageException(ErrorCode.GENERATED_IMAGE_EXCEPTION);
        } finally {
            if (lockedCredit != null) {
                creditService.releaseLock(user);
            }
        }
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
            FloorPlan houseFloorPlan = requireHouseFloorPlan(house);

            // 침대 ID 찾기
            Optional<Long> bedId = furnitureService.findBedId(generateImageRequest.selectiveIds());

            // 복층이 아닌 경우 침대 추가
            if (!houseFloorPlan.getStructure().equals(Structure.DUPLEX) && bedId.isPresent()) {
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
                                houseFloorPlan.getForm().getDescription(), priorityIdList.get(i).tagNameKr(), user.getName()));
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
                        GenerateImageType.FULL_FUNNEL
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
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_REQUEST);
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

    private List<BannerStyleAnswerChip> parseStyleAnswerChips(String styleAnswerChipsJson) {
        if (styleAnswerChipsJson == null || styleAnswerChipsJson.isBlank()) {
            return List.of();
        }
        try {
            List<BannerStyleAnswerChip> chips = objectMapper.readValue(styleAnswerChipsJson, STYLE_ANSWER_CHIP_TYPE);
            if (chips == null) {
                return List.of();
            }
            return chips.stream().filter(Objects::nonNull).toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    private String resolveFloorPlanImageUrl(FloorPlan floorPlan, String floorPlanView) {
        List<FloorPlanImageItem> images = floorPlanImageJsonCodec.read(floorPlan.getImagesJson());
        if (images.isEmpty()) {
            return floorPlan.getUrl();
        }

        String normalizedView = floorPlanView == null ? "" : floorPlanView.trim();
        if (!normalizedView.isEmpty()) {
            Optional<String> matchedUrl = images.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> item.url() != null && !item.url().isBlank())
                    .filter(item -> item.view() != null && item.view().trim().equalsIgnoreCase(normalizedView))
                    .map(FloorPlanImageItem::url)
                    .findFirst();
            if (matchedUrl.isPresent()) {
                return matchedUrl.get();
            }
        }

        return images.stream()
                .filter(Objects::nonNull)
                .map(FloorPlanImageItem::url)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(floorPlan.getUrl());
    }

    private String resolveFloorPlanImageUrlStrict(FloorPlan floorPlan, String floorPlanView) {
        List<FloorPlanImageItem> images = floorPlanImageJsonCodec.read(floorPlan.getImagesJson());
        String normalizedView = floorPlanView == null ? "" : floorPlanView.trim();

        if (normalizedView.isEmpty()) {
            throw new HouseException(ErrorCode.INVALID_FLOOR_PLAN_VIEW);
        }

        return images.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.url() != null && !item.url().isBlank())
                .filter(item -> item.view() != null && item.view().trim().equalsIgnoreCase(normalizedView))
                .map(FloorPlanImageItem::url)
                .findFirst()
                .orElseThrow(() -> new HouseException(ErrorCode.INVALID_FLOOR_PLAN_VIEW));
    }

    private List<String> buildReferenceImageUrls(
            Banner banner,
            BannerStyleAnswerChip selectedChip,
            String floorPlanImageUrl
    ) {
        LinkedHashSet<String> urls = new LinkedHashSet<>();
        if (floorPlanImageUrl != null && !floorPlanImageUrl.isBlank()) {
            urls.add(floorPlanImageUrl);
        }

        Map<Long, CurationRawProduct> bannerRawProductsById = new LinkedHashMap<>();
        banner.getBannerRawProducts().stream()
                .map(mapping -> mapping != null ? mapping.getCurationRawProduct() : null)
                .filter(Objects::nonNull)
                .forEach(rawProduct -> bannerRawProductsById.put(rawProduct.getId(), rawProduct));

        Long selectedChipRawProductId = selectedChip.curationRawProductId();
        if (selectedChipRawProductId != null) {
            CurationRawProduct selectedChipRawProduct = bannerRawProductsById.get(selectedChipRawProductId);
            if (selectedChipRawProduct == null) {
                selectedChipRawProduct = curationRawProductRepository.findById(selectedChipRawProductId)
                        .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT));
            }
            if (selectedChipRawProduct.getProductImageUrl() != null
                    && !selectedChipRawProduct.getProductImageUrl().isBlank()) {
                urls.add(selectedChipRawProduct.getProductImageUrl());
            }
        }

        if (banner.getBannerImageUrl() != null && !banner.getBannerImageUrl().isBlank()) {
            urls.add(banner.getBannerImageUrl());
        }

        bannerRawProductsById.values().forEach(rawProduct -> {
            if (Objects.equals(rawProduct.getId(), selectedChipRawProductId)) {
                return;
            }
            if (rawProduct.getProductImageUrl() != null && !rawProduct.getProductImageUrl().isBlank()) {
                urls.add(rawProduct.getProductImageUrl());
            }
        });

        return List.copyOf(urls);
    }

    private List<String> buildStyleReferenceImageUrls(Banner style, String floorPlanImageUrl) {
        LinkedHashSet<String> urls = new LinkedHashSet<>();
        if (floorPlanImageUrl != null && !floorPlanImageUrl.isBlank()) {
            urls.add(floorPlanImageUrl);
        }
        if (style.getBannerImageUrl() != null && !style.getBannerImageUrl().isBlank()) {
            urls.add(style.getBannerImageUrl());
        }

        style.getBannerRawProducts().stream()
                .map(mapping -> mapping != null ? mapping.getCurationRawProduct() : null)
                .filter(Objects::nonNull)
                .map(CurationRawProduct::getProductImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .forEach(urls::add);

        return List.copyOf(urls);
    }

    private String buildBannerPrompt(
            Banner banner,
            BannerStyleAnswerChip selectedChip,
            FloorPlan floorPlan
    ) {
        List<String> parts = new ArrayList<>();
        if (banner.getStylePrompt() != null && !banner.getStylePrompt().isBlank()) {
            parts.add(banner.getStylePrompt());
        }
        if (selectedChip.selectedPrompt() != null && !selectedChip.selectedPrompt().isBlank()) {
            parts.add(selectedChip.selectedPrompt());
        }
        if (floorPlan.getFloorPlanPrompt() != null && !floorPlan.getFloorPlanPrompt().isBlank()) {
            parts.add(floorPlan.getFloorPlanPrompt());
        }
        return String.join("\n\n", parts);
    }

    private String buildStylePrompt(Banner style, FloorPlan floorPlan) {
        List<String> parts = new ArrayList<>();
        if (style.getStylePrompt() != null && !style.getStylePrompt().isBlank()) {
            parts.add(style.getStylePrompt());
        }
        if (floorPlan.getFloorPlanPrompt() != null && !floorPlan.getFloorPlanPrompt().isBlank()) {
            parts.add(floorPlan.getFloorPlanPrompt());
        }
        return String.join("\n\n", parts);
    }

    private List<Long> buildCombinedFurnitureIds(Activity activity, List<Long> requestFurnitureIds) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (requestFurnitureIds != null) {
            ids.addAll(requestFurnitureIds);
        }

        List<ActivityFurniture> activityMappings =
                activityFurnitureRepository.findAllByActivityOrderByPriorityAscIdAsc(activity);
        activityMappings.stream()
                .map(ActivityFurniture::getFurniture)
                .filter(Objects::nonNull)
                .map(Furniture::getId)
                .filter(Objects::nonNull)
                .forEach(ids::add);

        return List.copyOf(ids);
    }

    private List<String> buildV4ReferenceImageUrls(String floorPlanImageUrl, List<FurnitureTag> furnitureTags) {
        LinkedHashSet<String> urls = new LinkedHashSet<>();
        if (floorPlanImageUrl != null && !floorPlanImageUrl.isBlank()) {
            urls.add(floorPlanImageUrl);
        }
        furnitureTags.stream()
                .map(FurnitureTag::getFurnitureUrl)
                .filter(url -> url != null && !url.isBlank())
                .forEach(urls::add);
        return List.copyOf(urls);
    }

    private String buildV4Prompt(FloorPlan floorPlan, Tag selectedTag, List<FurnitureTag> furnitureTags) {
        List<String> parts = new ArrayList<>();
        if (floorPlan.getFloorPlanPrompt() != null && !floorPlan.getFloorPlanPrompt().isBlank()) {
            parts.add(floorPlan.getFloorPlanPrompt());
        }
        if (selectedTag.getTagPrompt() != null && !selectedTag.getTagPrompt().isBlank()) {
            parts.add(selectedTag.getTagPrompt());
        }
        furnitureTags.stream()
                .map(FurnitureTag::getFurniturePrompt)
                .filter(prompt -> prompt != null && !prompt.isBlank())
                .forEach(parts::add);
        return String.join("\n\n", parts);
    }

    private List<String> buildProductReferenceImageUrls(
            String floorPlanImageUrl,
            List<CurationRawProduct> selectedProducts
    ) {
        LinkedHashSet<String> urls = new LinkedHashSet<>();
        if (floorPlanImageUrl != null && !floorPlanImageUrl.isBlank()) {
            urls.add(floorPlanImageUrl);
        }
        selectedProducts.stream()
                .map(CurationRawProduct::getProductImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .forEach(urls::add);
        return List.copyOf(urls);
    }

    private String buildProductBasedPrompt(FloorPlan floorPlan, List<CurationRawProduct> selectedProducts) {
        List<String> parts = new ArrayList<>();
        if (floorPlan.getFloorPlanPrompt() != null && !floorPlan.getFloorPlanPrompt().isBlank()) {
            parts.add(floorPlan.getFloorPlanPrompt());
        }
        parts.add("주어진 도면 구조와 원근을 유지하고, 참고 상품들을 실제 실내 배치처럼 자연스럽게 배치해주세요.");
        parts.add("동선과 크기 비율을 지키고, 가구 간 간섭 없이 현실적인 인테리어 결과를 생성해주세요.");

        selectedProducts.stream()
                .map(product -> product.getProductName())
                .filter(name -> name != null && !name.isBlank())
                .forEach(name -> parts.add("반영 상품: " + name));

        return String.join("\n\n", parts);
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
        FloorPlan floorPlan = requireHouseFloorPlan(houseById);
        // 평형
        String equilibrium = floorPlan.getEquilibrium().getDescription();
        // 집 형태
        String houseForm = floorPlan.getForm().getDescription();

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
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_REQUEST);
        }
    }

    private FloorPlan requireHouseFloorPlan(House house) {
        return houseFloorPlanRepository.findHouseFloorPlanByHouseId(house.getId())
                .map(HouseFloorPlan::getFloorPlan)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));
    }

}
