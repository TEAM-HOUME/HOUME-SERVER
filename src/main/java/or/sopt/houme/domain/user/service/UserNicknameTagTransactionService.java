package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.model.entity.CreditStatus;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.SocialType;
import or.sopt.houme.domain.user.model.entity.User;
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
    private final CreditRepository creditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User createSocialUserWithNicknameTag(
            SignupSession signupSession,
            String name,
            String nickname,
            String nicknameTag,
            Gender gender,
            LocalDate birthday
    ) {
        User savedUser = userRepository.saveAndFlush(
                User.builder()
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateUserFromSignUpV2(nickname, nicknameTag, birthday, gender);
        userRepository.saveAndFlush(user);
        createSignUpCredits(user);
        return user.getDisplayName();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User updateMyPageProfile(
            Long userId,
            String nickname,
            String nicknameTag,
            Gender gender,
            LocalDate birthday
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateMyPageProfile(nickname, nicknameTag, birthday, gender);
        return userRepository.saveAndFlush(user);
    }

    private void createSignUpCredits(User user) {
        try {
            List<Credit> newCredits = IntStream.range(0, SIGN_UP_CREDIT_COUNT)
                    .mapToObj(i -> Credit.builder()
                            .status(CreditStatus.ACTIVE)
                            .user(user)
                            .build())
                    .toList();
            creditRepository.saveAll(newCredits);
        } catch (Exception e) {
            throw new CreditException(ErrorCode.CREDIT_CREATE_EXCEPTION);
        }
    }
}
