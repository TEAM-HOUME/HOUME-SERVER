package or.sopt.houme.domain.house.repository.taste;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.mapping.HouseTaste;
import or.sopt.houme.taste.infra.persistence.TasteJpaEntity;
import or.sopt.houme.tastetag.infra.persistence.TasteTagJpaEntity;
import or.sopt.houme.tag.infra.persistence.TagJpaEntity;
import or.sopt.houme.tag.infra.persistence.TagQueryRepository;
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

/**
 * 태그 클러스터 조인 조회(QueryDSL) 특성화 테스트. 헥사고날 전환(#582)으로
 * {@code TagRepositoryImpl} → {@link TagQueryRepository} 로 이관되었고, 조회 결과는 {@link TagJpaEntity} 이다.
 */
@DataJpaTest
@Import({TagQueryRepository.class, QuerydslConfig.class})
@ActiveProfiles("test")
class TagRepositoryImplTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private TagQueryRepository tagQueryRepository;

    private User mockUser;
    private House mockHouse;
    private GenerateImage mockGenerateImage;
    private TasteJpaEntity mockTaste;
    private HouseTaste mockHouseTaste;
    private TasteTagJpaEntity mockTasteTag;
    private TagJpaEntity mockTag;

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
                .activity(Activity.READING)
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
        mockTaste = TasteJpaEntity.builder()
                .url("https://test.com/taste.png")
                .filename("taste.png")
                .originalFilename("origin-taste.png")
                .fileExtension("png")
                .build();
        em.persist(mockTaste);

        // 🏠-🎨 매핑
        mockHouseTaste = HouseTaste.builder()
                .houseId(mockHouse.getId())
                .tasteId(mockTaste.getId())
                .build();
        em.persist(mockHouseTaste);

        // 🔖 태그 생성
        mockTag = TagJpaEntity.builder()
                .tagName("모던")
                .priority(1)
                .tagNameKr("모던 인테리어")
                .tagPrompt("프롬프트")
                .build();
        em.persist(mockTag);

        // 🎨-🔖 매핑
        mockTasteTag = TasteTagJpaEntity.forInsert(mockTaste.getId(), mockTag.getId());
        em.persist(mockTasteTag);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("userId와 imageId로 tag 조회 성공")
    void findTagByUserIdAndImageId_success() {
        // when
        Optional<TagJpaEntity> result = tagQueryRepository.findTagByUserIdAndImageId(mockUser.getId(), mockGenerateImage.getId());

        // then
        assertThat(result).isPresent();
        TagJpaEntity tag = result.get();
        assertThat(tag.getTagName()).isEqualTo("모던");
    }

    @Test
    @DisplayName("존재하지 않는 imageId 조회 시 empty 반환")
    void findTagByUserIdAndImageId_invalidImage() {
        // when
        Optional<TagJpaEntity> result = tagQueryRepository.findTagByUserIdAndImageId(mockUser.getId(), 999L);

        // then
        assertThat(result).isEmpty();
    }
}
