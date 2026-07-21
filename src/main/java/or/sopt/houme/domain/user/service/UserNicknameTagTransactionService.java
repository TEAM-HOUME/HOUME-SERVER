package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.credit.application.CreditUseCase;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.SocialType;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;
import or.sopt.houme.domain.user.model.entity.UserStatus;
import or.sopt.houme.domain.user.model.entity.record.SignupSession;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CreditException;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserNicknameTagTransactionService {

    private static final int SIGN_UP_CREDIT_COUNT = 5;

    private final UserRepository userRepository;
    private final CreditUseCase creditUseCase;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserJpaEntity createSocialUserWithNicknameTag(
            SignupSession signupSession,
            String name,
            String nickname,
            String nicknameTag,
            Gender gender,
            LocalDate birthday
    ) {
        UserJpaEntity savedUser = userRepository.saveAndFlush(
                UserJpaEntity.builder()
                        .password(null)
                        .email(signupSession.email())
                        .name(name)
                        .nickname(nickname)
                        .nicknameTag(nicknameTag)
                        .birthday(birthday)
                        .gender(gender)
                        .role(Role.ROLE_USER)
                        .socialType(SocialType.KAKAO)
                        .status(UserStatus.ACTIVE)
                        .hasGeneratedImage(Boolean.FALSE)
                        .build()
        );
        createSignUpCredits(savedUser);
        return savedUser;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String completeUserSignUpV2(
            Long userId,
            String nickname,
            String nicknameTag,
            Gender gender,
            LocalDate birthday
    ) {
        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateUserFromSignUpV2(nickname, nicknameTag, birthday, gender);
        userRepository.saveAndFlush(user);
        createSignUpCredits(user);
        return user.getDisplayName();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserJpaEntity updateMyPageProfile(
            Long userId,
            String nickname,
            String nicknameTag,
            Gender gender,
            LocalDate birthday
    ) {
        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateMyPageProfile(nickname, nicknameTag, birthday, gender);
        return userRepository.saveAndFlush(user);
    }

    private void createSignUpCredits(UserJpaEntity user) {
        creditUseCase.grant(user.getId(), SIGN_UP_CREDIT_COUNT);
    }
}
