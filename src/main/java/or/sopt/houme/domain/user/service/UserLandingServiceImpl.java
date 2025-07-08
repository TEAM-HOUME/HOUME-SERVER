package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.domain.user.valid.RefreshTokenValidator;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLandingServiceImpl implements UserLandingService {

    private final UserRepository userRepository;
    private final RefreshTokenValidator refreshTokenValidator;

    @Override
    public Boolean getHasGeneratedImage(HttpServletRequest request){

        Long findUserIdByRefreshToken = refreshTokenValidator.validateRefreshToken(request);

        User findUser = userRepository.findById(findUserIdByRefreshToken)
                .orElseThrow(()-> new UserException(ErrorCode.USER_NOT_FOUND));

        return findUser.getHasGeneratedImage();
    }
}
