package or.sopt.houme.domain.generateImage.facade;

import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.generateImage.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImage.service.imageGenerationLog.ImageGenerationLogService;
import or.sopt.houme.domain.generateImage.service.imageGenerationLog.ImageGenerationTransactionService;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.openai.facade.OpenAiFacade;
import or.sopt.houme.domain.prompt.dto.PromptFurnitureListDTO;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.domain.taste.service.TagService;
import or.sopt.houme.domain.taste.service.TasteService;
import or.sopt.houme.domain.taste.service.TasteTagService;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.domain.user.service.UserService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[FloorPlan Service Test]")
class GenerateImageFacadeTest {

    @InjectMocks
    GenerateImageFacade generateImageFacade;

    @Mock
    OpenAiFacade openAiFacade;

    @Mock
    HouseService houseService;

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

        House house = House.builder()
                .id(1L)
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .activity(Activity.READING)
                .equilibrium(Equilibrium.UNDER_5)
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

        when(tasteTagService.getPriorityId(generateImageRequest.moodBoardIds()))
                .thenReturn(tag);

        PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(
                1L, tag.getId(), house.getEquilibrium(),
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

        when(generateImageService.createGenerateImage(imageUploadResponseDTO, house)).thenReturn(generateImage);

        // When
        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImage(user, generateImageRequest);

        // Then
        assertThat(imageInfoResponse).isNotNull();
        assertThat(imageInfoResponse.tagName()).isEqualTo(tag.getTagNameKr());
        assertThat(imageInfoResponse.imageUrl()).isEqualTo(imageLink);
        assertThat(imageInfoResponse.houseForm()).isEqualTo(house.getForm().getDescription());
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

        House house = House.builder()
                .id(1L)
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .activity(Activity.REMOTE_WORK)
                .equilibrium(Equilibrium.UNDER_5)
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

        Tag tag = Tag.builder()
                .id(1L)
                .priority(1)
                .tagName("modern")
                .tagNameKr("모던")
                .tagPrompt("취향 프롬프트")
                .build();

        when(houseService.updateHouseActivity(generateImageRequest.houseId(), Activity.valueOf(generateImageRequest.activity()))).thenReturn(house);

        // 침대 Id 찾기
        when(furnitureService.findBedId(generateImageRequest.selectiveIds())).thenReturn(Optional.empty());

        when(tasteTagService.getPriorityId(generateImageRequest.moodBoardIds()))
                .thenReturn(tag);

        PromptRequestDTO promptRequestDTO = PromptRequestDTO.of(
                1L, tag.getId(), house.getEquilibrium(),
                PromptFurnitureListDTO.of(generateImageRequest.selectiveIds())
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

        when(generateImageService.createGenerateImage(imageUploadResponseDTO, house)).thenReturn(generateImage);
        when(tasteService.getTasteList(moodBoardIds)).thenReturn(tasteList);
        when(tagService.findTagByTasteId(moodBoardIds.get(0))).thenReturn(tag);
        when(tagService.findTagByTasteId(moodBoardIds.get(1))).thenReturn(tag);
        when(tasteTagService.findDistinctTagsByTasteIds(moodBoardIds)).thenReturn(tagList);

        // When
        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImageByFastApi(user, generateImageRequest);

        // Then
        assertThat(imageInfoResponse).isNotNull();
        assertThat(imageInfoResponse.tagName()).isEqualTo(tag.getTagNameKr());
        assertThat(imageInfoResponse.imageUrl()).isEqualTo(imageLink);
        assertThat(imageInfoResponse.houseForm()).isEqualTo(house.getForm().getDescription());
        assertThat(imageInfoResponse.name()).isEqualTo(user.getName());
    }
}
