package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.model.entity.record.SignupSession;
import or.sopt.houme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UserNicknameTagTransactionServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CreditRepository creditRepository = mock(CreditRepository.class);
    private final UserNicknameTagTransactionService service = new UserNicknameTagTransactionService(
            userRepository,
            creditRepository
    );

    @Test
    @DisplayName("소셜 회원가입 v2는 가입 크레딧 5개를 생성한다")
    void createSocialUserWithNicknameTag_createsFiveCredits() {
        SignupSession signupSession = SignupSession.of(1L, "test@houme.kr", "카카오닉네임");
        User savedUser = User.builder().id(1L).email("test@houme.kr").build();

        given(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User.class))).willReturn(savedUser);

        service.createSocialUserWithNicknameTag(
                signupSession,
                "새닉네임",
                "새닉네임",
                "#1234",
                Gender.MALE,
                LocalDate.of(2000, 1, 1)
        );

        verify(creditRepository, times(1))
                .saveAll(argThat(credits -> credits instanceof java.util.Collection<?> c && c.size() == 5));
    }

    @Test
    @DisplayName("소셜 회원가입 v2는 카카오 닉네임을 name으로, 입력 닉네임을 nickname으로 저장한다")
    void createSocialUserWithNicknameTag_savesKakaoNicknameToName() {
        SignupSession signupSession = SignupSession.of(1L, "test@houme.kr", "카카오닉네임");
        given(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        User savedUser = service.createSocialUserWithNicknameTag(
                signupSession,
                "카카오닉네임",
                "서비스닉네임",
                "#1234",
                Gender.MALE,
                LocalDate.of(2000, 1, 1)
        );

        org.junit.jupiter.api.Assertions.assertAll(
                () -> org.junit.jupiter.api.Assertions.assertEquals("카카오닉네임", savedUser.getName()),
                () -> org.junit.jupiter.api.Assertions.assertEquals("서비스닉네임", savedUser.getNickname()),
                () -> org.junit.jupiter.api.Assertions.assertEquals("#1234", savedUser.getNicknameTag())
        );
    }

    @Test
    @DisplayName("자체 회원가입 v2 완료는 가입 크레딧 5개를 생성한다")
    void completeUserSignUpV2_createsFiveCredits() {
        User user = User.builder().id(1L).email("test@houme.kr").build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.saveAndFlush(user)).willReturn(user);

        service.completeUserSignUpV2(
                1L,
                "새닉네임",
                "#1234",
                Gender.MALE,
                LocalDate.of(2000, 1, 1)
        );

        verify(creditRepository, times(1))
                .saveAll(argThat(credits -> credits instanceof java.util.Collection<?> c && c.size() == 5));
    }
}
