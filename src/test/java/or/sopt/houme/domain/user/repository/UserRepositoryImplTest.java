package or.sopt.houme.domain.user.repository;

import jakarta.persistence.EntityManager;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.global.config.QuerydslConfig;
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

    @Test
    @DisplayName("사용자의 ACTIVE 상태인 크레딧 개수를 올바르게 카운트한다")
    void countByMemberIdAndStatus_success() {
        // given
        User mockUser = User.builder()
                .name("테스트유저")
                .birthday(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .email("test@example.com")
                .password("encodedPassword")
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .credits(new ArrayList<>())
                .houses(new ArrayList<>())
                .build();
        em.persist(mockUser); // persist 먼저!

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
}
