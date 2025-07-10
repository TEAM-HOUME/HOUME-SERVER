package or.sopt.houme.domain.house.repository;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.entity.Type;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({HouseCustomRepositoryImpl.class, QuerydslConfig.class})
@ActiveProfiles("test")
class HouseCustomRepositoryImplTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private HouseCustomRepositoryImpl houseCustomRepositoryImpl;

    private User mockUser;
    private House mockHouse;
    private GenerateImage mockGenerateImage;

    @BeforeEach
    void setUp() {
        // 🧪 User 생성 및 저장
        mockUser = User.builder()
                .name("테스트유저")
                .birthday(LocalDate.of(1999, 12, 31))
                .gender(Gender.MALE)
                .email("mock@example.com")
                .password("encoded123")
                .hasGeneratedImage(true)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();
        em.persist(mockUser);

        // 🏠 House 생성 및 저장
        mockHouse = House.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .activity(Activity.RELAXING)
                .user(mockUser)
                .isValid(true)
                .build();
        em.persist(mockHouse);

        // 🖼️ GenerateImage 생성 및 저장
        mockGenerateImage = GenerateImage.builder()
                .url("https://test.com/image.png")
                .filename("image.png")
                .originalFilename("origin.png")
                .fileExtension("png")
                .type(Type.PNG)
                .house(mockHouse)
                .build();
        em.persist(mockGenerateImage);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("✅ userId와 imageId로 house 조회 성공")
    void findHouseByUserIdAndImageId_success() {
        // when
        Optional<House> result = houseCustomRepositoryImpl.findHouseByUserIdAndImageId(mockUser.getId(), mockGenerateImage.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(mockHouse.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(mockUser.getId());
    }

    @Test
    @DisplayName("❌ 잘못된 imageId로 조회 시 empty 반환")
    void findHouseByUserIdAndImageId_invalidImage() {
        // when
        Optional<House> result = houseCustomRepositoryImpl.findHouseByUserIdAndImageId(mockUser.getId(), 999L);

        // then
        assertThat(result).isEmpty();
    }
}
