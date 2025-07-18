package or.sopt.houme.domain.generateImage.service;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("[GenerateImage Service] Test")
class GenerateImageServiceImplTest {

    @Autowired
    private GenerateImageService generateImageService;

    @Autowired
    private GenerateImageRepository generateImageRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;
    private House savedHouse;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(
                User.builder()
                        .name("test_user")
                        .birthday(LocalDate.of(2001, 1, 10))
                        .gender(Gender.MALE)
                        .email("example.com")
                        .password(null)
                        .hasGeneratedImage(false)
                        .socialType(SocialType.KAKAO)
                        .status(UserStatus.ACTIVE)
                        .role(Role.ROLE_USER)
                        .build()
        );

        savedHouse = houseRepository.save(
                House.builder()
                        .form(Form.OFFICETEL)
                        .structure(Structure.OPEN_ONE_ROOM)
                        .equilibrium(Equilibrium.UNDER_5)
                        .isValid(true)
                        .user(savedUser)
                        .build()
        );
    }

    @Test
    @DisplayName("도면 이미지를 생성 후 저장 할 수 있다.")
    void createGenerateImage() {
        // Given
        String fileName = "fileName";
        String originalFilename = "originalFilename";
        String imageLink = "imageLink";
        String contentType = "JPG";

        ImageUploadResponseDTO from = ImageUploadResponseDTO.from(fileName, originalFilename, imageLink, contentType);

        // When
        GenerateImage generateImage = generateImageService.createGenerateImage(from, savedHouse);

        // Then
        assertThat(generateImage).isNotNull();
        assertThat(generateImage.getFilename()).isEqualTo(fileName);
        assertThat(generateImage.getFileExtension()).isEqualTo(contentType);
        assertThat(generateImage.getOriginalFilename()).isEqualTo(originalFilename);
    }

    @Test
    @DisplayName("도면 ID로 조회 할 수 있다.")
    void findGenerateImageById() {
        // Given
        String fileName = "fileName";
        String originalFilename = "originalFilename";
        String imageLink = "imageLink";
        String contentType = "JPG";

        GenerateImage generateImage = GenerateImage.builder()
                .filename(fileName)
                .originalFilename(originalFilename)
                .url(imageLink)
                .fileExtension(contentType)
                .build();

        GenerateImage saveImage = generateImageRepository.save(generateImage);

        // When
        GenerateImage generateImage1 = generateImageService.findGenerateImage(saveImage.getId());

        // Then
        assertThat(generateImage1).isNotNull();
        assertThat(generateImage1.getFilename()).isEqualTo(fileName);
        assertThat(generateImage1.getFileExtension()).isEqualTo(contentType);
        assertThat(generateImage1.getOriginalFilename()).isEqualTo(originalFilename);
        assertThat(generateImage1.getUrl()).isEqualTo(imageLink);
    }
}
