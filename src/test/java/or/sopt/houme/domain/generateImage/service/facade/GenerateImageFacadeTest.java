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
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.service.GeminiImageService;
import or.sopt.houme.domain.generateImage.presentation.dto.request.BannerGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.response.BannerGenerateImageResponse;
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
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
                GenerateImageType.RECOMMEND
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
        when(houseFloorPlanRepository.findHouseFloorPlanByHouseId(house.getId()))
                .thenReturn(Optional.of(HouseFloorPlan.builder().house(house).floorPlan(floorPlan).isReverse(false).build()));

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
                GenerateImageType.RECOMMEND
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
        )).thenReturn(BannerGenerateImageResponse.of(999L));

        BannerGenerateImageResponse response = generateImageFacade.generateBannerImageByGemini(user, request);

        assertThat(response.imageId()).isEqualTo(999L);
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
}
