package or.sopt.houme.domain.taste.repository;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.entity.mapping.HouseTaste;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.domain.taste.entity.TasteTag;
import or.sopt.houme.domain.taste.repository.tag.TagRepositoryImpl;
import or.sopt.houme.domain.user.entity.*;
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
@Import({TagRepositoryImpl.class, QuerydslConfig.class})
@ActiveProfiles("test")
class TagRepositoryImplTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private TagRepositoryImpl tagRepositoryImpl;

    private User mockUser;
    private House mockHouse;
    private GenerateImage mockGenerateImage;
    private Taste mockTaste;
    private HouseTaste mockHouseTaste;
    private TasteTag mockTasteTag;
    private Tag mockTag;

    @BeforeEach
    void setUp() {
        // 👤 사용자 생성
        mockUser = User.builder()
                .name("테스트유저")
                .birthday(LocalDate.of(1995, 5, 5))
                .gender(Gender.MALE)
                .email("user@test.com")
                .password("pwd")
                .hasGeneratedImage(true)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();
        em.persist(mockUser);

        // 🏠 집 생성
        mockHouse = House.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .activity(Activity.RELAXING)
                .user(mockUser)
                .isValid(true)
                .build();
        em.persist(mockHouse);

        // 🖼️ 이미지 생성
        mockGenerateImage = GenerateImage.builder()
                .url("https://test.com/image.png")
                .filename("image.png")
                .originalFilename("origin.png")
                .fileExtension("png")
                .house(mockHouse)
                .build();
        em.persist(mockGenerateImage);

        // 🎨 취향 생성
        mockTaste = Taste.builder()
                .url("https://test.com/taste.png")
                .filename("taste.png")
                .originalFilename("origin-taste.png")
                .fileExtension("png")
                .tastePrompt("모던 인테리어")
                .build();
        em.persist(mockTaste);

        // 🏠-🎨 매핑
        mockHouseTaste = HouseTaste.builder()
                .house(mockHouse)
                .taste(mockTaste)
                .build();
        em.persist(mockHouseTaste);

        // 🔖 태그 생성
        mockTag = Tag.builder()
                .tagName("모던")
                .build();
        em.persist(mockTag);

        // 🎨-🔖 매핑
        mockTasteTag = TasteTag.builder()
                .taste(mockTaste)
                .tag(mockTag)
                .build();
        em.persist(mockTasteTag);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("userId와 imageId로 tag 조회 성공")
    void findTagByUserIdAndImageId_success() {
        // when
        Optional<Tag> result = tagRepositoryImpl.findTagByUserIdAndImageId(mockUser.getId(), mockGenerateImage.getId());

        // then
        assertThat(result).isPresent();
        Tag tag = result.get();
        assertThat(tag.getTagName()).isEqualTo("모던");
    }

    @Test
    @DisplayName("존재하지 않는 imageId 조회 시 empty 반환")
    void findTagByUserIdAndImageId_invalidImage() {
        // when
        Optional<Tag> result = tagRepositoryImpl.findTagByUserIdAndImageId(mockUser.getId(), 999L);

        // then
        assertThat(result).isEmpty();
    }
}
