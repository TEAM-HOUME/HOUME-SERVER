package or.sopt.houme.domain.user.repository;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import or.sopt.houme.domain.generatedImage.entity.GenerateImage;
import or.sopt.houme.domain.generatedImage.entity.Type;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.entity.mapping.HouseTaste;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.domain.taste.entity.TasteTag;
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
                .url("test-url.png")
                .type(Type.PNG)        // 예: Type.IMAGE 또는 Type.VIDEO (enum 값은 실제 정의에 따라)
                .house(mockHouse)        // 이미 생성한 mockHouse 객체 사용
                .build();
        em.persist(mockGenerateImage);

        mockHouseTaste = HouseTaste.builder()
                .house(mockHouse)   // 이미 생성된 House 객체
                .taste(mockTaste)   // 이미 생성된 Taste 객체
                .build();
        em.persist(mockHouseTaste);

        mockTaste = Taste.builder()
                .tasteImage("taste-modern.png")  // 예시 이미지 URL 또는 경로
                .build();
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
    @DisplayName("✅ 사용자 이미지 히스토리 조회 성공")
    void getUserImageHistory_Success() {

    }
}
