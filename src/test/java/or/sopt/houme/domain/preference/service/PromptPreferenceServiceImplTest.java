package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.preference.model.entity.GenerateImagePreference;
import or.sopt.houme.domain.preference.model.entity.Preference;
import or.sopt.houme.domain.preference.repository.GenerateImagePreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.user.model.entity.*;
import or.sopt.houme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[PromptPreference Service] Test")
class PromptPreferenceServiceImplTest {

    @Autowired
    GenerateImagePreferenceRepository generateImagePreferenceRepository;

    @Autowired
    PreferenceRepository preferenceRepository;

    @Autowired
    HouseRepository houseRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    GenerateImagePreferenceService generateImagePreferenceService;

    @Autowired
    GenerateImageRepository generateImageRepository;

    @Test
    @DisplayName("집 도면 좋아요를 생성 할 수 있다.")
    void createPromptPreference() {
        // Given
        Preference preference = Preference.of(true);
        preferenceRepository.save(preference);

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
        User save = userRepository.save(user);

        House house = House.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .isValid(true)
                .user(save)
                .build();
        House saveHouse = houseRepository.save(house);

        GenerateImage generateImage = GenerateImage.builder()
                .house(saveHouse)
                .url("https://www.example.com")
                .fileExtension("JPG")
                .originalFilename("image.jpg")
                .filename("image.jpg")
                .build();
        GenerateImage saveImage = generateImageRepository.save(generateImage);

        // When
        generateImagePreferenceService.toggleGenerateImagePreference(saveImage, preference.isLike());

        // Then
        List<GenerateImagePreference> all = generateImagePreferenceRepository.findAll();
        assertThat(all.size()).isEqualTo(1);
        assertThat(all.get(0).getGenerateImage()).isEqualTo(saveImage);
        assertThat(all.get(0).getPreference().isLike()).isEqualTo(preference.isLike());
    }
}