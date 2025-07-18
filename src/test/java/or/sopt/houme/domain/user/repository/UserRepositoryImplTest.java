package or.sopt.houme.domain.user.repository;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.entity.CreditStatus;
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
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserRepositoryImpl.class, QuerydslConfig.class})
@ActiveProfiles("test")
class UserRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    private User mockUser;
    private House mockHouse;
    private GenerateImage mockGenerateImage;
    private HouseTaste mockHouseTaste;
    private Taste mockTaste;
    private TasteTag mockTasteTag;
    private Tag mockTag;

    @BeforeEach
    void setUp() {
        // 1. 유저 생성
        mockUser = User.builder()
                .name("테스트유저")
                .birthday(LocalDate.of(1999, 12, 31))
                .gender(Gender.MALE)
                .email("mock@example.com")
                .password("encodedPassword123")
                .hasGeneratedImage(true)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();
        em.persist(mockUser);

        // 2. 태그 2개 생성
        Tag tagModern = Tag.builder()
                .tagName("모던")
                .tagPrompt("깔끔한 화이트톤의 거실")
                .tagNameKr("모던")
                .priority(1)
                .build();
        em.persist(tagModern);

        Tag tagVintage = Tag.builder()
                .tagName("빈티지")
                .tagPrompt("따뜻한 느낌의 원목 인테리어")
                .tagNameKr("빈티지")
                .priority(2)
                .build();
        em.persist(tagVintage);

        // 3. Taste 2개 생성 + 각각 Tag 연결
        Taste taste1 = Taste.builder()
                .url("https://example.com/taste1.png")
                .filename("taste1.png")
                .originalFilename("original-taste1.png")
                .fileExtension("png")
                .build();
        em.persist(taste1);

        Taste taste2 = Taste.builder()
                .url("https://example.com/taste2.png")
                .filename("taste2.png")
                .originalFilename("original-taste2.png")
                .fileExtension("png")
                .build();
        em.persist(taste2);

        em.persist(TasteTag.builder().taste(taste1).tag(tagModern).build());
        em.persist(TasteTag.builder().taste(taste2).tag(tagVintage).build());
        em.persist(TasteTag.builder().taste(taste2).tag(tagModern).build()); // taste2는 tag 2개

        // 4. House 2개 생성
        House house1 = House.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .activity(Activity.RELAXING)
                .user(mockUser)
                .isValid(true)
                .build();
        em.persist(house1);

        House house2 = House.builder()
                .form(Form.APARTMENT)
                .structure(Structure.DUPLEX)
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .activity(Activity.HOME_THEATER)
                .user(mockUser)
                .isValid(true)
                .build();
        em.persist(house2);

        // 5. HouseTaste로 Taste 연결
        em.persist(HouseTaste.builder().house(house1).taste(taste1).build());
        em.persist(HouseTaste.builder().house(house2).taste(taste2).build());

        // 6. GenerateImage 2개 생성
        GenerateImage generateImage1 = GenerateImage.builder()
                .url("https://example.com/image1.png")
                .filename("image1.png")
                .originalFilename("original-image1.png")
                .fileExtension("png")
                .house(house1)
                .build();
        em.persist(generateImage1);

        GenerateImage generateImage2 = GenerateImage.builder()
                .url("https://example.com/image2.png")
                .filename("image2.png")
                .originalFilename("original-image2.png")
                .fileExtension("png")
                .house(house2)
                .build();
        em.persist(generateImage2);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("사용자의 ACTIVE 상태인 크레딧 개수를 올바르게 카운트한다")
    void countByMemberIdAndStatus_success() {
        // given
        // ACTIVE 상태 크레딧
        em.persist(Credit.builder().user(mockUser).status(CreditStatus.ACTIVE).build());

        // EXPIRED 상태 크레딧
        em.persist(Credit.builder().user(mockUser).status(CreditStatus.EXPIRED).build());

        // REVOKED 상태 크레딧
        em.persist(Credit.builder().user(mockUser).status(CreditStatus.REVOKED).build());

        em.flush();
        em.clear();

        // when
        long count = userRepositoryImpl.countByMemberIdAndStatus(mockUser.getId());

        // then
        assertThat(count).isEqualTo(1L); // ✅ 실제 ACTIVE 상태는 1개
    }

    @Test
    @DisplayName("사용자 이미지 히스토리 조회 - 여러 이력 정상 조회 및 태그 우선순위 확인")
    void getUserImageHistory_Success() {
        // when
        List<UserImageHistoryDTO> result = userRepositoryImpl.getUserImageHistory(mockUser.getId());

        // then
        assertThat(result).hasSize(2);

        // 첫 번째 DTO
        UserImageHistoryDTO dto1 = result.get(0);
        assertThat(dto1.generatedImageUrl()).isEqualTo("https://example.com/image1.png");
        assertThat(dto1.tasteTag()).isEqualTo("모던");
        assertThat(dto1.equilibrium()).isEqualTo("5평 이하");
        assertThat(dto1.houseForm()).isEqualTo("오피스텔");

        // 두 번째 DTO
        UserImageHistoryDTO dto2 = result.get(1);
        assertThat(dto2.generatedImageUrl()).isEqualTo("https://example.com/image2.png");

        // dto2의 태그는 taste2가 tag 2개를 갖고 있으므로, 등장 횟수 높은 "모던" 우선
        assertThat(dto2.tasteTag()).isEqualTo("모던"); // 등장 횟수 기준으로 "모던" 우선
        assertThat(dto2.equilibrium()).isEqualTo("6~10평");
        assertThat(dto2.houseForm()).isEqualTo("아파트");
    }

    @Test
    @DisplayName("유저 ID로 이미지 히스토리 1건 조회 성공 - 여러 이미지 중 하나")
    void findImageHistoryById_Success() {
        // when
        Optional<GenerateImage> result = userRepositoryImpl.findImageHistoryById(mockUser.getId());

        // then
        assertThat(result).isPresent(); // Optional이 존재해야 함

        GenerateImage image = result.get();
        assertThat(image.getUrl()).isIn(
                "https://example.com/image1.png",
                "https://example.com/image2.png"
        );
        assertThat(image.getFilename()).isIn("image1.png", "image2.png");
    }
}
