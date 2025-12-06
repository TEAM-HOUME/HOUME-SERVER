package or.sopt.houme.domain.user.service;

import feign.FeignException;
import jakarta.servlet.http.Cookie;
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
import or.sopt.houme.domain.user.entity.UserStatus;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import or.sopt.houme.global.config.CookieConfig;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.config.KaKaoConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.CookieUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    private final CookieConfig cookieConfig;


    /**
     * 카카오 인증 서버에 인가코드를 요청하는 메서드입니다
     *
     * 실제로는 클라이언트 단으로 주소가 리다이렉트되어 인가코드를 클라이언트에서 파싱하여 넘겨줄 것이기 떄문에 해당 메서드는 사용되지 않습니다
     * 서버에서 로직의 유효성을 검사하기 위해 사용합니다
     *
     * 현재 fallback 로직이 구현되어있지 않습니다. 아직 Feign의 fallback factory에 대한 학습이 부족해서...
     * 앱잼기간내에 구현해보겠습니다 FIXME
     * */

    /**
     *
     *
     * */
    public String requestRedirect(HttpServletRequest request) {
        String redirectBase = resolveRedirectBase(request);
        String redirectUri = redirectBase + "/oauth/kakao/callback";

        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(kaKaoConfig.getScope(), StandardCharsets.UTF_8);

        log.info("소셜로그인 주소입니다: {}", encodedRedirect);

        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
                kaKaoConfig.getClientId(), encodedRedirect, encodedScope
        );
    }


    public Boolean kakaoLogin(String accessCode, HttpServletRequest request, HttpServletResponse response) {

        // 신규회원인지 검증하는 필드
        Boolean isNewUser = false;

        // 인가코드가 비어있다면 예외발생
        if (accessCode == null || accessCode.isEmpty()) {
            throw new UserException(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        log.info("인가코드가 비어있지 않습니다");
        // 인가코드를 받고 그걸 통해서 인증 액세스 토큰을 발급받습니다
        KaKaoOAuthTokenDTO authorizationCode;
        try {
            log.info("액세스 토큰 발급을 시작합니다");
            String redirectBase = resolveRedirectBase(request);
            String redirectUri = redirectBase + "/oauth/kakao/callback";
            authorizationCode = getKaKaoOAuthTokenDTO(accessCode, redirectUri);

            log.info("인가코드를 발급하기 위한 리다이렉트 주소입니다: {}", redirectUri);

        } catch (FeignException e) {
            log.info(e.getMessage());
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
//                  이름은 자체 회원가입시 입력 받습니다.
                    .password(null)
                    .email(userInfo.getKakao_account().getEmail())
                    .role(Role.ROLE_USER)
                    .socialType(SocialType.KAKAO)
                    .status(UserStatus.ACTIVE)
                    .hasGeneratedImage(Boolean.FALSE)
                    .build();

            userRepository.save(newUser);

            isNewUser = true;
        }

        // 그리고 회원 정보를 기반으로 액세스토큰을 발급하여 헤더에 넣습니다
        User byEmail = userRepository.findByEmail(userInfo.getKakao_account().getEmail())
                .orElseThrow(()-> new UserException(ErrorCode.USER_NOT_FOUND));

        String access = jwtUtil.createJwt("access", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String refresh = jwtUtil.createJwt("refresh", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());

        refreshTokenRepository.saveRefreshToken(byEmail.getId(),refresh,jwtConfig.getRefreshTokenValidityInSeconds());

        response.setHeader("access-token", access);

        CookieUtil.addSameSiteCookie(
                response,
                "refresh-token",
                refresh,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                cookieConfig.getDomain(),
                cookieConfig.isSecure(),
                cookieConfig.getSameSite()
        );

        return isNewUser;
    }


    /**
     * @param userDetails userDetails 에서 회원의 id를 받아서 그걸로 리프레시 토큰을 삭제합니다
     * @param request 헤더를 블랙리스트에 추가하기 위해 필요합니다
     *
     *
     * 로그아웃 로직은 다음과 같습니다
     * 1. 회원의 식별자를 통해 서버에서 리프레시 토큰을 삭제합니다
     * 2. 액세스 토큰을 찾아서 블랙리스트에 추가합니다
     * 2-1. 이때 남은 액세스 토큰의 만료기간을 TTL로 설정합니다
     * 3. JWTFilter 에서 블랙리스트에 해당 토큰이 있는지 탐색하고 있다면 그에 맞는 예외를 반환합니다
     * */
    public void logout(CustomUserDetails userDetails, HttpServletRequest request,HttpServletResponse response) {

        Long id = userDetails.getUser().getId();
        refreshTokenRepository.deleteById(id);

        String authorizationHeader = request.getHeader(jwtConfig.getHeader());

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }

        String accessToken = authorizationHeader.substring(7).trim();

        String jti = jwtUtil.getJti(accessToken);
        long expiration = jwtUtil.getRemainingExpiration(accessToken);

        blacklistTokenRepository.save(jti, expiration);

        CookieUtil.deleteCookie(
                response,
                "refresh-token",
                cookieConfig.getDomain(),
                cookieConfig.isSecure(),
                cookieConfig.getSameSite()
        );
    }



    private KaKaoOAuthTokenDTO getKaKaoOAuthTokenDTO(String accessCode, String redirectUri) {
        return kaKaoOAuthClient.getToken(
                "authorization_code",
                kaKaoConfig.getClientId(),
                redirectUri,
                accessCode
        );
    }

    private String resolveRedirectBase(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (StringUtils.hasText(origin)) {
            return origin;
        }

        String scheme = java.util.Objects.toString(request.getHeader("X-Forwarded-Proto"), request.getScheme());
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (StringUtils.hasText(forwardedHost)) {
            return scheme + "://" + forwardedHost;
        }

        String host = request.getServerName();
        int port = request.getServerPort();
        boolean isDefault = ("http".equalsIgnoreCase(scheme) && port == 80) || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + host + (isDefault ? "" : ":" + port);
    }
}
