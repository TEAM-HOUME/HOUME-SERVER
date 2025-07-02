package or.sopt.houme.domain.user.service;

import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.client.KaKaoUserInfoClient;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.domain.user.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.domain.user.entity.SocialType;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TokenException;
import or.sopt.houme.global.api.handler.UserException;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.config.KaKaoConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.CookieUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final KaKaoOAuthClient kaKaoOAuthClient;
    private final KaKaoUserInfoClient kaKaoUserInfoClient;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;

    private final KaKaoConfig kaKaoConfig;


    /**
     * 카카오 인증 서버에 인가코드를 요청하는 메서드입니다
     *
     * 실제로는 클라이언트 단으로 주소가 리다이렉트되어 인가코드를 클라이언트에서 파싱하여 넘겨줄 것이기 떄문에 해당 메서드는 사용되지 않습니다
     * 서버에서 로직의 유효성을 검사하기 위해 사용합니다
     *
     * 현재 fallback 로직이 구현되어있지 않습니다. 아직 Feign의 fallback factory에 대한 학습이 부족해서...
     * 앱잼기간내에 구현해보겠습니다 FIXME
     * */
    public String requestRedirect() {
        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                kaKaoConfig.getClientId(), kaKaoConfig.getRedirectUri()
        );
    }


    public void kakaoLogin(String accessCode, HttpServletResponse response) {

        // 인가코드가 비어있다면 예외발생
        if (accessCode == null || accessCode.isEmpty()) {
            throw new UserException(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        // 인가코드를 받고 그걸 통해서 인증 액세스 토큰을 발급받습니다
        KaKaoOAuthTokenDTO authorizationCode;
        try {
            authorizationCode = getKaKaoOAuthTokenDTO(accessCode);
        } catch (FeignException e) {
            throw new UserException(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        // 그리고 액세스 토큰을 이용하여 회원 정보를 가져옵니다
        KaKaoUserInfoResponse userInfo;
        try {
            userInfo = kaKaoUserInfoClient.getUserInfo(
                    "Bearer "+authorizationCode.getAccess_token());
        }catch (FeignException e) {
            throw new UserException(ErrorCode.KAKAO_ACCESSTOKEN_INVALID);
        }

        // 만약 해당 이메일을 통해 회원가입된 회원이 존재하지 않는다면, 새로운 회원을 생성합니다
        Boolean userExist = userRepository.existsByEmail(userInfo.getKakao_account().getEmail());

        if (userExist == Boolean.FALSE) {
            User newUser = User.builder()
                    .name(userInfo.getProperties().getNickname())
                    .password(null)
                    .email(userInfo.getKakao_account().getEmail())
                    .role(Role.ROLE_USER)
                    .socialType(SocialType.KAKAO)
                    .build();

            userRepository.save(newUser);
        }

        // 그리고 회원 정보를 기반으로 액세스토큰을 발급하여 헤더에 넣습니다
        User byEmail = userRepository.findByEmail(userInfo.getKakao_account().getEmail())
                .orElseThrow(()-> new UserException(ErrorCode.USER_NOT_FOUND));

        String access = jwtUtil.createJwt("access", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String refresh = jwtUtil.createJwt("refresh", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());

        refreshTokenRepository.saveRefreshToken(byEmail.getId(),refresh,jwtConfig.getRefreshTokenValidityInSeconds());

        response.setHeader("access-token", access);

        Cookie refreshCookie = CookieUtil.createSecureCookie("refresh-token",
                refresh,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                false);

        response.addCookie(refreshCookie);
    }


    public void logout(CustomUserDetails userDetails, HttpServletRequest request, HttpServletResponse response) {

        log.info("로그아웃 시작");

        Long id = userDetails.getUser().getId();
        refreshTokenRepository.deleteById(id); // 1. 리프레시 토큰 삭제

        String authorizationHeader = request.getHeader(jwtConfig.getHeader());

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Authorization header is missing or invalid during logout.");
            return;
        }

        String accessToken = authorizationHeader.substring(7).trim();

        // 2. jti 추출
        String jti = jwtUtil.getJti(accessToken);
        log.info("jti: {}", jti);

        // 3. 액세스 토큰 남은 시간 계산
        long expiration = jwtUtil.getRemainingExpiration(accessToken);

        // 4. 블랙리스트 등록
        blacklistTokenRepository.save(jti, expiration);
        log.info("Access token added to blacklist: {}", jti);
    }



    private KaKaoOAuthTokenDTO getKaKaoOAuthTokenDTO(String accessCode) {

        return kaKaoOAuthClient.getToken(
                "authorization_code",
                kaKaoConfig.getClientId(),
                kaKaoConfig.getRedirectUri(),
                accessCode
        );
    }
}
