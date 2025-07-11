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
        mockUser = User.builder()
                .name("테스트유저")
                .birthday(LocalDate.of(1999, 12, 31))
                .gender(Gender.MALE)
                .email("mock@example.com")
                .password("encodedPassword123")
                .hasGeneratedImage(true)  // 생성된 이미지가 있다고 가정
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .credits(new ArrayList<>())  // 초기화된 빈 크레딧 목록
                .houses(new ArrayList<>())   // 초기화된 빈 집 목록
                .build();
        em.persist(mockUser);

        mockHouse = House.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.UNDER_5)
                .activity(Activity.RELAXING)
                .user(mockUser)
                .isValid(true)
                .build();
        em.persist(mockHouse);

        mockGenerateImage = GenerateImage.builder()
                .url("https://example.com/image.png")
                .filename("image.png")
                .originalFilename("original-image.png")
                .fileExtension("png")
                .house(mockHouse) // 이미 생성된 House mock 객체
                .build();
        em.persist(mockGenerateImage);

        mockTaste = Taste.builder()
                .url("https://example.com/taste-image.png")
                .filename("taste-image.png")
                .originalFilename("original-taste-image.png")
                .fileExtension("png")
                .tastePrompt("깔끔한 화이트톤의 거실 인테리어") // 예시 프롬프트
                .build();
        em.persist(mockTaste);

        Tag mockTag = Tag.builder()
                .tagName("모던")
                .build();
        em.persist(mockTag);

        mockTasteTag = TasteTag.builder()
                .taste(mockTaste)
                .tag(mockTag)
                .build();
        em.persist(mockTasteTag);

        mockHouseTaste = HouseTaste.builder()
                .house(mockHouse)   // 이미 생성된 House 객체
                .taste(mockTaste)   // 이미 생성된 Taste 객체
                .build();
        em.persist(mockHouseTaste);

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
    @DisplayName("사용자 이미지 히스토리 조회 성공")
    void getUserImageHistory_Success() {
        // when
        List<UserImageHistoryDTO> result = userRepositoryImpl.getUserImageHistory(mockUser.getId());

        // then
        assertThat(result).hasSize(1);
        UserImageHistoryDTO dto = result.get(0);
        assertThat(dto.generatedImageUrl()).isEqualTo("https://example.com/image.png");
        assertThat(dto.tasteTag()).isEqualTo("모던");
        assertThat(dto.equilibrium()).isEqualTo("UNDER_5");
        assertThat(dto.houseForm()).isEqualTo("OFFICETEL");
    }

    @Test
    @DisplayName("유저 ID로 이미지 히스토리 1건 조회 성공")
    void findImageHistoryById_Success() {
        // when
        Optional<GenerateImage> result = userRepositoryImpl.findImageHistoryById(mockUser.getId());

        // then
        assertThat(result).isPresent(); // Optional이 존재해야 함
        assertThat(result.get().getUrl()).isEqualTo("https://example.com/image.png");
        assertThat(result.get().getFilename()).isEqualTo("image.png");
    }
}
