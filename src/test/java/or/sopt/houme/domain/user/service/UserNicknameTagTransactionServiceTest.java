package or.sopt.houme.domain.user.service;

import or.sopt.houme.credit.application.CreditUseCase;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;
import or.sopt.houme.domain.user.model.entity.record.SignupSession;
import or.sopt.houme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UserNicknameTagTransactionServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CreditUseCase creditUseCase = mock(CreditUseCase.class);
    private final UserNicknameTagTransactionService service = new UserNicknameTagTransactionService(
            userRepository,
            creditUseCase
    );

    @Test
    @DisplayName("소셜 회원가입 v2는 가입 크레딧 5개를 생성한다")
    void createSocialUserWithNicknameTag_createsFiveCredits() {
        SignupSession signupSession = SignupSession.of(1L, "test@houme.kr", "카카오닉네임");
        UserJpaEntity savedUser = UserJpaEntity.builder().id(1L).email("test@houme.kr").build();

        given(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(UserJpaEntity.class))).willReturn(savedUser);

        service.createSocialUserWithNicknameTag(
                signupSession,
                "새닉네임",
                "새닉네임",
                "#1234",
                Gender.MALE,
                LocalDate.of(2000, 1, 1)
        );

        verify(creditUseCase, times(1)).grant(eq(1L), eq(5));
    }

    @Test
    @DisplayName("자체 회원가입 v2 완료는 가입 크레딧 5개를 생성한다")
    void completeUserSignUpV2_createsFiveCredits() {
        UserJpaEntity user = UserJpaEntity.builder().id(1L).email("test@houme.kr").build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.saveAndFlush(user)).willReturn(user);

        service.completeUserSignUpV2(
                1L,
                "새닉네임",
                "#1234",
                Gender.MALE,
                LocalDate.of(2000, 1, 1)
        );

        verify(creditUseCase, times(1)).grant(eq(1L), eq(5));
    }
}
