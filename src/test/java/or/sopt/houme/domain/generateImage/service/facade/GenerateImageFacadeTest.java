package or.sopt.houme.domain.generateImage.service.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.model.entity.CreditStatus;
import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.furniture.model.entity.ActivityFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.repository.ActivityFurnitureRepository;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.service.GeminiImageService;
import or.sopt.houme.domain.generateImage.presentation.dto.request.BannerGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageV4Request;
import or.sopt.houme.domain.generateImage.presentation.dto.request.ProductGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.response.BannerGenerateImageResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.GenerateImageV4Response;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImage.service.GenerateImageTransactionService;
import or.sopt.houme.domain.generateImage.service.imageGenerationLog.ImageGenerationLogService;
import or.sopt.houme.domain.generateImage.service.imageGenerationLog.ImageGenerationTransactionService;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.repository.HouseFloorPlanRepository;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.generateImage.service.openai.facade.OpenAiFacade;
import or.sopt.houme.domain.generateImage.service.prompt.PromptService;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.model.taste.entity.Taste;
import or.sopt.houme.domain.house.service.taste.TagService;
import or.sopt.houme.domain.house.service.taste.TasteService;
import or.sopt.houme.domain.house.service.taste.TasteTagService;
import or.sopt.houme.domain.user.model.entity.*;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.api.handler.HouseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[FloorPlan Service Test]")
class GenerateImageFacadeTest {

    @InjectMocks
    GenerateImageFacade generateImageFacade;

    @Mock
    OpenAiFacade openAiFacade;

    @Mock
    GeminiImageService geminiImageService;

    @Mock
    PromptService promptService;

    @Mock
    BannerRepository bannerRepository;

    @Mock
    FloorPlanRepository floorPlanRepository;

    @Mock
    CurationRawProductRepository curationRawProductRepository;

    @Mock
    FurnitureTagRepository furnitureTagRepository;

    @Mock
    ActivityFurnitureRepository activityFurnitureRepository;

    @Mock
    FloorPlanImageJsonCodec floorPlanImageJsonCodec;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    HouseService houseService;

    @Mock
    HouseFloorPlanRepository houseFloorPlanRepository;

    @Mock
    CreditService creditService;

    @Mock
    TasteTagService tasteTagService;

    @Mock
    UserService userService;

    @Mock
    GenerateImageService generateImageService;

    @Mock
    FurnitureService furnitureService;

    @Mock
    TasteService tasteService;

    @Mock
    ImageGenerationLogService imageGenerationLogService;

    @Mock
    ImageGenerationTransactionService imageGenerationTransactionService;

    @Mock
    GenerateImageTransactionService generateImageTransactionService;

    @Mock
    TagService tagService;

    @Test
    @DisplayName("받은 데이터들을 토대로 JAVA로 OpenAI를 사용해 이미지를 받을 수 있다.")
    void generateImage() {
        // Given
        User user = User.builder()
                .name("test_user")
                .birthday(LocalDate.of(2001, 1, 10))
                .gender(Gender.MALE)
                .email("example.com")
                .password(null)
                .hasGeneratedImage(false)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();

        FloorPlan floorPlan = FloorPlan.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .build();
        House house = House.builder()
                .id(1L)
                .activity(Activity.READING)
                .user(user)
                .isValid(true)
                .build();

        GenerateImageRequest generateImageRequest = new GenerateImageRequest(
                1L, "UNDER_5", new GenerateImageRequest.FloorPlanInfo(1L, false),
                List.of(3L), "READING", new ArrayList<>()
        );

        Tag tag = Tag.builder()
                .id(1L)
                .priority(1)
                .tagName("modern")
                .tagNameKr("모던")
                .tagPrompt("취향 프롬프트")
                .build();

        when(houseService.updateHouseActivity(generateImageRequest.houseId(), Activity.valueOf(generateImageRequest.activity()))).thenReturn(house);
        when(houseFloorPlanRepository.findHouseFloorPlanByHouseId(house.getId()))
                .thenReturn(Optional.of(HouseFloorPlan.builder().house(house).floorPlan(floorPlan).isReverse(false).build()));

        when(tasteTagService.getPriorityId(generateImageRequest.moodBoardIds()))
                .thenReturn(tag);

        PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(
                1L, tag.getId(), floorPlan.getEquilibrium(),
                PromptFurnitureListDTO.of(List.of())
        );

        String filename = "filename";
        String originalFilename = "original_filename";
        String imageLink = "image_link";
        String contentType = "content_type";
        String pullPrompt = "pull_prompt";

        ImageUploadResponseDTO imageUploadResponseDTO = ImageUploadResponseDTO.from(
                filename, originalFilename, imageLink, contentType
        );

        imageUploadResponseDTO.setPullPrompt(pullPrompt);

        when(openAiFacade.makeImage(promptRequestDTO)).thenReturn(imageUploadResponseDTO);

        GenerateImage generateImage = GenerateImage.builder()
                .id(1L)
                .url(imageLink)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(contentType)
                .house(house)
                .build();

        when(generateImageService.createGenerateImage(
                imageUploadResponseDTO,
                house,
                GenerateImageType.FULL_FUNNEL
        )).thenReturn(generateImage);

        // When
        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImage(user, generateImageRequest);

        // Then
        assertThat(imageInfoResponse).isNotNull();
        assertThat(imageInfoResponse.tagName()).isEqualTo(tag.getTagNameKr());
        assertThat(imageInfoResponse.imageUrl()).isEqualTo(imageLink);
        assertThat(imageInfoResponse.houseForm()).isEqualTo(floorPlan.getForm().getDescription());
        assertThat(imageInfoResponse.name()).isEqualTo(user.getName());
    }

    @Test
    @DisplayName("받은 데이터들을 토대로 FastAPI로 OpenAI를 사용해 이미지를 받을 수 있다.")
    void generateImageV2() {
        // Given
        User user = User.builder()
                .name("test_user")
                .birthday(LocalDate.of(2001, 1, 10))
                .gender(Gender.MALE)
                .email("example.com")
                .password(null)
                .hasGeneratedImage(false)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();

        FloorPlan floorPlan = FloorPlan.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .build();
        House house = House.builder()
                .id(1L)
                .activity(Activity.REMOTE_WORK)
                .user(user)
                .isValid(true)
                .build();

        Taste taste1 = Taste.builder()
                .id(1L)
                .url("exm")
                .originalFilename("exm")
                .filename("exm")
                .fileExtension("exm")
                .build();

        Taste taste2 = Taste.builder()
                .id(2L)
                .url("exm1")
                .originalFilename("exm1")
                .filename("exm1")
                .fileExtension("exm1")
                .build();

        GenerateImageRequest generateImageRequest = new GenerateImageRequest(
                1L, "UNDER_5", new GenerateImageRequest.FloorPlanInfo(1L, false),
                List.of(1L, 2L), "READING", List.of(1L)
        );

        Credit lockedCredit = null;

        Tag tag = Tag.builder()
                .id(1L)
                .priority(1)
                .tagName("modern")
                .tagNameKr("모던")
                .tagPrompt("취향 프롬프트")
                .build();

        when(tasteTagService.getPriorityId(generateImageRequest.moodBoardIds()))
                .thenReturn(tag);

        PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(
                1L, tag.getId(), floorPlan.getEquilibrium(),
                PromptFurnitureListDTO.of(generateImageRequest.selectiveIds())
        );

        String filename = "filename";
        String originalFilename = "original_filename";
        String imageLink = "image_link";
        String contentType = "content_type";
        String pullPrompt = "pull_prompt";

        ImageInfoResponse tempImageInfoResponse = new ImageInfoResponse(
                1L, imageLink, false, floorPlan.getEquilibrium().getDescription(), floorPlan.getForm().getDescription(), tag.getTagNameKr(), user.getName()
                );

        ImageUploadResponseDTO imageUploadResponseDTO = ImageUploadResponseDTO.from(
                filename, originalFilename, imageLink, contentType
        );

        imageUploadResponseDTO.setPullPrompt(pullPrompt);

        when(openAiFacade.makeImageByFastApi(promptRequestDTO)).thenReturn(imageUploadResponseDTO);

        GenerateImage generateImage = GenerateImage.builder()
                .id(1L)
                .url(imageLink)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(contentType)
                .house(house)
                .build();

        List<Long> moodBoardIds = List.of(1L, 2L);
        List<Taste> tasteList = List.of(taste1, taste2);
        List<Tag> tagList = List.of(tag);

        when(tasteService.getTasteList(moodBoardIds)).thenReturn(tasteList);
        when(tagService.findTagByTasteId(moodBoardIds.get(0))).thenReturn(tag);
        when(tagService.findTagByTasteId(moodBoardIds.get(1))).thenReturn(tag);
        when(tasteTagService.findDistinctTagsByTasteIds(moodBoardIds)).thenReturn(tagList);
        when(generateImageTransactionService.saveAllDataAndConfirmCredit(
                user,
                lockedCredit,
                generateImageRequest,
                imageUploadResponseDTO,
                tag,
                Activity.valueOf(generateImageRequest.activity()),
                GenerateImageType.FULL_FUNNEL
        )).thenReturn(tempImageInfoResponse);

        // When
        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImageByFastApi(user, generateImageRequest);

        // Then
        assertThat(imageInfoResponse).isNotNull();
        assertThat(imageInfoResponse.tagName()).isEqualTo(tag.getTagNameKr());
        assertThat(imageInfoResponse.imageUrl()).isEqualTo(imageLink);
        assertThat(imageInfoResponse.houseForm()).isEqualTo(floorPlan.getForm().getDescription());
        assertThat(imageInfoResponse.name()).isEqualTo(user.getName());
    }

    @Test
    @DisplayName("배너 템플릿 이미지 생성은 saveBannerImageAndConfirmCredit 경로를 호출해 LIST 이미지 저장 플로우를 실행한다")
    void generateBannerImageByGemini_callsSaveBannerImageAndConfirmCredit() throws Exception {
        User user = User.builder()
                .id(1L)
                .name("test_user")
                .build();

        Credit lockedCredit = Credit.builder()
                .id(10L)
                .status(CreditStatus.PENDING)
                .user(user)
                .build();

        Banner banner = Banner.builder()
                .id(100L)
                .bannerType(BannerType.BANNER)
                .bannerImageUrl("https://banner-image")
                .stylePrompt("배너 스타일 프롬프트")
                .styleAnswerChipsJson("[{\"id\":1}]")
                .bannerRawProducts(List.of())
                .build();

        FloorPlan floorPlan = FloorPlan.builder()
                .id(11L)
                .url("https://floorplan-image")
                .floorPlanPrompt("도면 프롬프트")
                .imagesJson("[]")
                .build();

        BannerGenerateImageRequest request = new BannerGenerateImageRequest(
                100L,
                1L,
                11L,
                "TOP",
                false
        );

        ImageUploadResponseDTO imageUploadResponseDTO = ImageUploadResponseDTO.from(
                "generated.webp",
                "generated-original.webp",
                "https://generated-image",
                "image/webp"
        );

        when(creditService.tryLockAndGetCredit(user)).thenReturn(lockedCredit);
        when(bannerRepository.findByIdWithRawProducts(100L, BannerType.BANNER, false)).thenReturn(Optional.of(banner));
        when(floorPlanRepository.findById(11L)).thenReturn(Optional.of(floorPlan));
        when(objectMapper.readValue(eq("[{\"id\":1}]"), any(TypeReference.class)))
                .thenReturn(List.of(new BannerStyleAnswerChip(1L, 1, "답변", "선택 프롬프트", null)));
        when(floorPlanImageJsonCodec.read("[]")).thenReturn(List.of());
        when(geminiImageService.createImageWithReferences(any(), any()))
                .thenReturn(imageUploadResponseDTO);
        when(generateImageTransactionService.saveBannerImageAndConfirmCredit(
                eq(user),
                eq(lockedCredit),
                eq(banner),
                eq(11L),
                eq(false),
                any(),
                eq(imageUploadResponseDTO)
        )).thenReturn(BannerGenerateImageResponse.of(999L, "https://generated-image", false));

        BannerGenerateImageResponse response = generateImageFacade.generateBannerImageByGemini(user, request);

        assertThat(response.imageId()).isEqualTo(999L);
        assertThat(response.imageUrl()).isEqualTo("https://generated-image");
        assertThat(response.isMirror()).isFalse();
        verify(generateImageTransactionService).saveBannerImageAndConfirmCredit(
                eq(user),
                eq(lockedCredit),
                eq(banner),
                eq(11L),
                eq(false),
                any(),
                eq(imageUploadResponseDTO)
        );
    }

    @Test
    @DisplayName("V4 이미지 생성은 활동 연계 가구를 포함한 reference 이미지로 호출 후 저장한다")
    void generateImageV4ByGemini_callsSaveV4ImageAndConfirmCredit() {
        User user = User.builder()
                .id(1L)
                .name("test_user")
                .build();

        Credit lockedCredit = Credit.builder()
                .id(10L)
                .status(CreditStatus.PENDING)
                .user(user)
                .build();

        GenerateImageV4Request request = new GenerateImageV4Request(
                11L,
                "창가 뷰",
                true,
                List.of(1L, 2L),
                "REMOTE_WORK",
                List.of(7L)
        );

        Tag selectedTag = Tag.builder()
                .id(100L)
                .tagPrompt("스타일 프롬프트")
                .build();

        FloorPlan floorPlan = FloorPlan.builder()
                .id(11L)
                .url("https://floorplan-default")
                .floorPlanPrompt("도면 프롬프트")
                .imagesJson("[]")
                .build();

        Furniture selectedFurniture = Furniture.builder().id(7L).build();
        Furniture activityFurniture = Furniture.builder().id(8L).build();

        ActivityFurniture mapping = ActivityFurniture.builder()
                .id(1L)
                .activity(Activity.REMOTE_WORK)
                .furniture(activityFurniture)
                .priority(1)
                .build();

        FurnitureTag selectedFurnitureTag = FurnitureTag.builder()
                .id(1L)
                .furniture(selectedFurniture)
                .tag(selectedTag)
                .furnitureUrl("https://furniture-7")
                .furniturePrompt("선택 가구 프롬프트")
                .priority(1)
                .searchKeyword("k1")
                .build();
        FurnitureTag activityFurnitureTag = FurnitureTag.builder()
                .id(2L)
                .furniture(activityFurniture)
                .tag(selectedTag)
                .furnitureUrl("https://furniture-8")
                .furniturePrompt("활동 가구 프롬프트")
                .priority(2)
                .searchKeyword("k2")
                .build();

        ImageUploadResponseDTO imageUploadResponseDTO = ImageUploadResponseDTO.from(
                "generated.webp",
                "generated-original.webp",
                "https://generated-image",
                "image/webp"
        );

        when(creditService.tryLockAndGetCredit(user)).thenReturn(lockedCredit);
        when(tasteTagService.getPriorityId(request.moodBoardIds())).thenReturn(selectedTag);
        when(floorPlanRepository.findById(11L)).thenReturn(Optional.of(floorPlan));
        when(activityFurnitureRepository.findAllByActivityOrderByPriorityAscIdAsc(Activity.REMOTE_WORK))
                .thenReturn(List.of(mapping));
        when(furnitureTagRepository.findAllByFurnitureIdInAndTagId(List.of(7L, 8L), 100L))
                .thenReturn(List.of(selectedFurnitureTag, activityFurnitureTag));
        when(floorPlanImageJsonCodec.read("[]"))
                .thenReturn(List.of(FloorPlanImageItem.create("https://floorplan-view", "file", "orig", "png", 1, "창가 뷰")));
        when(geminiImageService.createImageWithReferences(any(), any()))
                .thenReturn(imageUploadResponseDTO);
        when(generateImageTransactionService.saveV4ImageAndConfirmCredit(
                eq(user),
                eq(lockedCredit),
                eq(11L),
                eq(true),
                any(),
                eq(imageUploadResponseDTO),
                eq(Activity.REMOTE_WORK),
                eq(List.of(7L, 8L)),
                eq(List.of(1L, 2L))
        )).thenReturn(GenerateImageV4Response.of(999L, "https://generated-image", true));

        GenerateImageV4Response response = generateImageFacade.generateImageV4ByGemini(user, request);

        assertThat(response.imageId()).isEqualTo(999L);
        assertThat(response.imageUrl()).isEqualTo("https://generated-image");
        assertThat(response.isMirror()).isTrue();
        verify(geminiImageService).createImageWithReferences(
                any(),
                argThat(urls -> urls.size() == 3
                        && urls.get(0).equals("https://floorplan-view")
                        && urls.contains("https://furniture-7")
                        && urls.contains("https://furniture-8"))
        );
        verify(generateImageTransactionService).saveV4ImageAndConfirmCredit(
                eq(user),
                eq(lockedCredit),
                eq(11L),
                eq(true),
                any(),
                eq(imageUploadResponseDTO),
                eq(Activity.REMOTE_WORK),
                eq(List.of(7L, 8L)),
                eq(List.of(1L, 2L))
        );
    }

    @Test
    @DisplayName("V4 이미지 생성은 존재하지 않는 floorPlanView 입력 시 예외를 던진다")
    void generateImageV4ByGemini_throwsWhenFloorPlanViewNotFound() {
        User user = User.builder()
                .id(1L)
                .name("test_user")
                .build();

        Credit lockedCredit = Credit.builder()
                .id(10L)
                .status(CreditStatus.PENDING)
                .user(user)
                .build();

        GenerateImageV4Request request = new GenerateImageV4Request(
                11L,
                "창가 뷰",
                true,
                List.of(1L, 2L),
                "REMOTE_WORK",
                List.of(7L)
        );

        Tag selectedTag = Tag.builder()
                .id(100L)
                .tagPrompt("스타일 프롬프트")
                .build();

        FloorPlan floorPlan = FloorPlan.builder()
                .id(11L)
                .url("https://floorplan-default")
                .floorPlanPrompt("도면 프롬프트")
                .imagesJson("[]")
                .build();

        ActivityFurniture mapping = ActivityFurniture.builder()
                .id(1L)
                .activity(Activity.REMOTE_WORK)
                .furniture(Furniture.builder().id(8L).build())
                .priority(1)
                .build();

        FurnitureTag furnitureTag = FurnitureTag.builder()
                .id(1L)
                .furniture(Furniture.builder().id(7L).build())
                .tag(selectedTag)
                .furnitureUrl("https://furniture-7")
                .furniturePrompt("선택 가구 프롬프트")
                .priority(1)
                .searchKeyword("k1")
                .build();

        when(creditService.tryLockAndGetCredit(user)).thenReturn(lockedCredit);
        when(tasteTagService.getPriorityId(request.moodBoardIds())).thenReturn(selectedTag);
        when(floorPlanRepository.findById(11L)).thenReturn(Optional.of(floorPlan));
        when(activityFurnitureRepository.findAllByActivityOrderByPriorityAscIdAsc(Activity.REMOTE_WORK))
                .thenReturn(List.of(mapping));
        when(furnitureTagRepository.findAllByFurnitureIdInAndTagId(List.of(7L, 8L), 100L))
                .thenReturn(List.of(furnitureTag));
        when(floorPlanImageJsonCodec.read("[]"))
                .thenReturn(List.of(FloorPlanImageItem.create("https://floorplan-view", "file", "orig", "png", 1, "정면")));

        assertThatThrownBy(() -> generateImageFacade.generateImageV4ByGemini(user, request))
                .isInstanceOf(HouseException.class)
                .extracting(ex -> ((HouseException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FLOOR_PLAN_VIEW);

        verify(creditService).rollbackCreditPending(lockedCredit);
        verify(geminiImageService, never()).createImageWithReferences(any(), any());
        verify(generateImageTransactionService, never()).saveV4ImageAndConfirmCredit(
                any(),
                any(),
                anyLong(),
                anyBoolean(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    @DisplayName("선택 상품 기반 이미지 생성은 도면+상품 이미지로 생성 후 저장한다")
    void generateImageByProducts_callsGeminiAndSaves() {
        User user = User.builder().id(1L).name("test_user").build();
        Credit lockedCredit = Credit.builder().id(10L).status(CreditStatus.PENDING).user(user).build();
        ProductGenerateImageRequest request = new ProductGenerateImageRequest(
                11L,
                "창가 뷰",
                true,
                List.of(1L, 2L, 3L)
        );

        FloorPlan floorPlan = FloorPlan.builder()
                .id(11L)
                .floorPlanPrompt("도면 프롬프트")
                .imagesJson("[]")
                .build();

        CurationRawProduct p1 = CurationRawProduct.builder().id(1L).productName("소파").productImageUrl("https://p1").build();
        CurationRawProduct p2 = CurationRawProduct.builder().id(2L).productName("책상").productImageUrl("https://p2").build();
        CurationRawProduct p3 = CurationRawProduct.builder().id(3L).productName("조명").productImageUrl("https://p3").build();

        ImageUploadResponseDTO imageUploadResponseDTO = ImageUploadResponseDTO.from(
                "generated.webp",
                "generated-original.webp",
                "https://generated-image",
                "image/webp"
        );

        when(creditService.tryLockAndGetCredit(user)).thenReturn(lockedCredit);
        when(floorPlanRepository.findById(11L)).thenReturn(Optional.of(floorPlan));
        when(curationRawProductRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(p1, p2, p3));
        when(floorPlanImageJsonCodec.read("[]"))
                .thenReturn(List.of(FloorPlanImageItem.create("https://floorplan-view", "file", "orig", "png", 1, "창가 뷰")));
        when(geminiImageService.createImageWithReferences(any(), any())).thenReturn(imageUploadResponseDTO);
        when(generateImageTransactionService.saveProductImageAndConfirmCredit(
                eq(user), eq(lockedCredit), eq(11L), eq(true), any(), eq(imageUploadResponseDTO), eq(List.of(p1, p2, p3))
        )).thenReturn(GenerateImageV4Response.of(999L, "https://generated-image", true));

        GenerateImageV4Response response = generateImageFacade.generateImageByProducts(user, request);

        assertThat(response.imageId()).isEqualTo(999L);
        assertThat(response.imageUrl()).isEqualTo("https://generated-image");
        assertThat(response.isMirror()).isTrue();
        verify(geminiImageService).createImageWithReferences(
                any(),
                argThat(urls -> urls.size() == 4
                        && urls.get(0).equals("https://floorplan-view")
                        && urls.contains("https://p1")
                        && urls.contains("https://p2")
                        && urls.contains("https://p3"))
        );
        verify(generateImageTransactionService).saveProductImageAndConfirmCredit(
                eq(user), eq(lockedCredit), eq(11L), eq(true), any(), eq(imageUploadResponseDTO), eq(List.of(p1, p2, p3))
        );
    }
}
