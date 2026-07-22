package or.sopt.houme.domain.user.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.credit.application.CreditUseCase;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.user.domain.User;
import or.sopt.houme.user.domain.port.out.UserImageHistoryQueryPort;
import or.sopt.houme.user.domain.port.out.UserRepositoryPort;
import or.sopt.houme.domain.user.presentation.controller.dto.ImageHistoriesResultPageResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageGeneratedImageV2Response;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageProfileResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final int SIGN_UP_CREDIT_COUNT = 5;
    private static final String USER_NICKNAME_TAG_UNIQUE_CONSTRAINT = "uk_user_nickname_nickname_tag";

    private final UserRepositoryPort userRepositoryPort;
    private final UserImageHistoryQueryPort userImageHistoryQueryPort;
    private final CreditUseCase creditUseCase;
    private final NicknameService nicknameService;
    private final UserNicknameTagTransactionService userNicknameTagTransactionService;

    @Override
    @Transactional(readOnly = true)
    public MyPageInfoResponse getMyPageInfo(User user) {
        User findUser = findUser(user);
        String name = findUser.getDisplayName();
        Long creditCount = creditUseCase.countActive(findUser.getId());
        return MyPageInfoResponse.of(findUser.getId(), name, creditCount, findUser.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public MyPageProfileResponse getMyPageProfile(User user) {
        User findUser = findUser(user);
        return MyPageProfileResponse.from(findUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserImageHistoryListResponse getUserImageHistoryList(User user) {
        User findUser = findUser(user);
        // #582: house·generateImage 엔티티 그래프 조립은 infra 어댑터(UserImageHistoryQueryAdapter)로 이관
        return userImageHistoryQueryPort.getUserImageHistoryList(findUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MyPageGeneratedImageV2Response getUserGeneratedImageHistoryListV2(User user) {
        User findUser = findUser(user);
        return userImageHistoryQueryPort.getUserGeneratedImageHistoryListV2(findUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ImageHistoriesResultPageResponse getImageHistoryResultPage(User user, Long houseId) {
        User findUser = findUser(user);
        return userImageHistoryQueryPort.getImageHistoryResultPage(findUser.getId(), findUser.getDisplayName(), houseId);
    }

    @Override
    public String updateUser(User user, String name, Gender gender, LocalDate birthday) {

        User findUser = findUser(user);
        findUser.updateUserFromSignUp(name, birthday, gender);
        userRepositoryPort.save(findUser);

        return createSignUpCreditAndGetDisplayName(findUser);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String updateUserV2(User user, String nickname, Gender gender, LocalDate birthday) {
        Long userId = user.getId();
        findUser(user);

        return executeWithNicknameTagRetry(nickname, nicknameTag ->
                userNicknameTagTransactionService.completeUserSignUpV2(
                        userId,
                        nickname,
                        nicknameTag,
                        gender,
                        birthday
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public MyPageProfileResponse updateMyPageProfile(User user, String nickname, Gender gender, LocalDate birthday) {
        Long userId = user.getId();
        findUser(user);

        if (nickname == null) {
            User updatedUser = userNicknameTagTransactionService.updateMyPageProfile(
                    userId,
                    null,
                    null,
                    gender,
                    birthday
            );
            return MyPageProfileResponse.from(updatedUser);
        }

        return executeWithNicknameTagRetry(nickname, nicknameTag -> {
            User updatedUser = userNicknameTagTransactionService.updateMyPageProfile(
                    userId,
                    nickname,
                    nicknameTag,
                    gender,
                    birthday
            );
            return MyPageProfileResponse.from(updatedUser);
        });
    }

    private <T> T executeWithNicknameTagRetry(String nickname, Function<String, T> nicknameTagCommand) {
        for (int attempt = 0; attempt < NicknameService.NICKNAME_TAG_RETRY_COUNT; attempt++) {
            String nicknameTag = nicknameService.generateNicknameTag(nickname);
            try {
                return nicknameTagCommand.apply(nicknameTag);
            } catch (DataIntegrityViolationException exception) {
                if (!isNicknameTagConstraintViolation(exception)) {
                    throw exception;
                }
            }
        }

        throw new UserException(ErrorCode.NICKNAME_TAG_GENERATION_FAILED);
    }

    private String createSignUpCreditAndGetDisplayName(User findUser) {
        creditUseCase.grant(findUser.getId(), SIGN_UP_CREDIT_COUNT);
        return findUser.getDisplayName();
    }

    // 이미지 생성 이력 저장
    @Transactional
    @Override
    public void updateHasGeneratedImage(User user) {
        user.updateHasGeneratedImage();

        userRepositoryPort.save(user);
    }

    private boolean isNicknameTagConstraintViolation(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolationException) {
                return USER_NICKNAME_TAG_UNIQUE_CONSTRAINT.equals(constraintViolationException.getConstraintName());
            }
            current = current.getCause();
        }
        return exception.getMessage() != null && exception.getMessage().contains(USER_NICKNAME_TAG_UNIQUE_CONSTRAINT);
    }

    private User findUser(User user) {
        return userRepositoryPort.findById(user.getId()).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }
}
