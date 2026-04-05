package or.sopt.houme.domain.generateImage.repository;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.user.model.entity.*;
import or.sopt.houme.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({GenerateImageRepositoryImpl.class, QuerydslConfig.class})
@ActiveProfiles("test")
class GenerateImageRepositoryImplTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private GenerateImageRepositoryImpl generateImageRepositoryImpl;

    private User user;
    private House house;
    private GenerateImage generateImage;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("테스트유저")
                .email("test@email.com")
                .password("1234")
                .birthday(LocalDate.of(1995, 5, 5))
                .gender(Gender.FEMALE)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();
        em.persist(user);

        house = House.builder()
                .user(user)
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .activity(Activity.REMOTE_WORK)
                .isValid(true)
                .build();
        em.persist(house);

        generateImage = GenerateImage.builder()
                .url("https://cdn.com/image.png")
                .filename("image.png")
                .originalFilename("origin-image.png")
                .fileExtension("png")
                .house(house)
                .build();
        em.persist(generateImage);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findMostRecentByUserId는 유저 기준 최신 생성 이미지를 반환한다")
    void findMostRecentByUserId_returnsLatestImage() {
        Optional<GenerateImage> result = generateImageRepositoryImpl.findMostRecentByUserId(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(generateImage.getId());
    }
}
