package or.sopt.houme.domain.user.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.model.entity.CreditStatus;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.user.infrastructure.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.infrastructure.client.KaKaoUserInfoClient;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.presentation.controller.dto.KakaoLoginResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.domain.user.presentation.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.domain.user.model.entity.*;
import or.sopt.houme.domain.user.model.entity.record.SignupSession;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.SignupSessionRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CreditException;
import or.sopt.houme.global.api.handler.UserException;
import or.sopt.houme.global.config.CookieConfig;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.config.KaKaoConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.CookieUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private static final int SIGN_UP_CREDIT_COUNT = 1;
    private static final String USER_NICKNAME_TAG_UNIQUE_CONSTRAINT = "uk_user_nickname_nickname_tag";

    private final KaKaoOAuthClient kaKaoOAuthClient;
    private final KaKaoUserInfoClient kaKaoUserInfoClient;
    private final UserRepository userRepository;
    private final CreditRepository creditRepository;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final SignupSessionRepository signupSessionRepository;
    private final NicknameService nicknameService;
    private final UserNicknameTagTransactionService userNicknameTagTransactionService;

    private final KaKaoConfig kaKaoConfig;
    private final CookieConfig cookieConfig;

    @Value("${auth.signup-token.ttl-seconds:600}")
    private long signupTokenTtlSeconds;

    /**
     * 카카오 인증 서버에 인가코드를 요청하는 메서드입니다
     *
     * 실제로는 클라이언트 단으로 주소가 리다이렉트되어 인가코드를 클라이언트에서 파싱하여 넘겨줄 것이기 떄문에 해당 메서드는 사용되지 않습니다
     * 서버에서 로직의 유효성을 검사하기 위해 사용합니다
     *
     * 현재 fallback 로직이 구현되어있지 않습니다. 아직 Feign의 fallback factory에 대한 학습이 부족해서...
     * 앱잼기간내에 구현해보겠습니다 FIXME
     * */
    public String requestRedirect(HttpServletRequest request, String env) {
        String redirectBase = resolveRedirectBase(request, env);
        String redirectUri = redirectBase + "/oauth/kakao/callback";

        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(kaKaoConfig.getScope(), StandardCharsets.UTF_8);

        log.info("소셜로그인 주소입니다: {}", encodedRedirect);

        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
                kaKaoConfig.getClientId(), encodedRedirect, encodedScope
        );
    }


    public KakaoLoginResponse kakaoLogin(String accessCode, String env, HttpServletRequest request, HttpServletResponse response) {

        // 인가코드가 비어있다면 예외발생
        if (accessCode == null || accessCode.isEmpty()) {
            throw new UserException(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        log.info("인가코드가 비어있지 않습니다");
        // 인가코드를 받고 그걸 통해서 인증 액세스 토큰을 발급받습니다
        KaKaoOAuthTokenDTO authorizationCode;
        try {
            log.info("액세스 토큰 발급을 시작합니다");
            String redirectBase = resolveRedirectBase(request, env);
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

        // 응답값에서 이메일을 파싱
        String email = Optional.ofNullable(userInfo.getKakao_account())
                .map(KaKaoUserInfoResponse.KakaoAccount::getEmail)
                .orElse(null);
        if (!StringUtils.hasText(email)) {
            throw new UserException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        // nickname 파싱
        String nickname = Optional.ofNullable(userInfo.getKakao_account())
                .map(KaKaoUserInfoResponse.KakaoAccount::getProfile)
                .map(KaKaoUserInfoResponse.KakaoAccount.Profile::getNickname)
                .orElse(null);

        Boolean userExist = userRepository.existsByEmail(email);

        // 회원 정보가 없는 경우 (이메일이 존재하지 않음) -> 임시토큰을 발급하여 반환합니다
        if (userExist == Boolean.FALSE) {
            String signupToken = UUID.randomUUID().toString().replace("-", "");

            SignupSession signupSession = SignupSession.of(
                    userInfo.getId(),
                    email,
                    nickname
            );
            signupSessionRepository.save(signupToken, signupSession, signupTokenTtlSeconds);

            return KakaoLoginResponse.newUser(signupToken, email, nickname);
        }

        // 회원정보가 존재하는 경우 -> 회원 정보를 기반으로 액세스토큰을 발급하여 헤더에 넣습니다
        User byEmail = userRepository.findByEmail(email)
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

        return KakaoLoginResponse.existingUser();
    }

    // Backward-compatible overloads for existing tests and callers
    public String requestRedirect(HttpServletRequest request) {
        return requestRedirect(request, null);
    }

    public KakaoLoginResponse kakaoLogin(String accessCode, HttpServletRequest request, HttpServletResponse response) {
        return kakaoLogin(accessCode, null, request, response);
    }

    @Transactional
    public String signUpWithToken(String signupToken, String name, Gender gender, LocalDate birthday, HttpServletResponse response) {
        return signUpWithTokenInternal(signupToken, name, null, null, gender, birthday, response);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String signUpWithTokenV2(String signupToken, String nickname, Gender gender, LocalDate birthday, HttpServletResponse response) {
        SignupSession signupSession = signupSessionRepository.consume(signupToken)
                .orElseThrow(() -> new UserException(ErrorCode.SIGNUP_TOKEN_INVALID));

        if (!StringUtils.hasText(signupSession.email())) {
            throw new UserException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        if (userRepository.existsByEmail(signupSession.email())) {
            throw new UserException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User savedUser = createUserWithNicknameTagRetry(signupSession, nickname, gender, birthday);
        issueTokens(savedUser, response);
        return savedUser.getDisplayName();
    }

    private String signUpWithTokenInternal(
            String signupToken,
            String name,
            String nickname,
            String nicknameTag,
            Gender gender,
            LocalDate birthday,
            HttpServletResponse response
    ) {
        SignupSession signupSession = signupSessionRepository.consume(signupToken)
                .orElseThrow(() -> new UserException(ErrorCode.SIGNUP_TOKEN_INVALID));

        if (!StringUtils.hasText(signupSession.email())) {
            throw new UserException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        if (userRepository.existsByEmail(signupSession.email())) {
            throw new UserException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User savedUser = userRepository.save(
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

        try {
            List<Credit> newCredits = IntStream.range(0, SIGN_UP_CREDIT_COUNT)
                    .mapToObj(i -> Credit.builder()
                            .status(CreditStatus.ACTIVE)
                            .user(savedUser)
                            .build())
                    .toList();
            creditRepository.saveAll(newCredits);
        } catch (Exception e) {
            throw new CreditException(ErrorCode.CREDIT_CREATE_EXCEPTION);
        }

        issueTokens(savedUser, response);

        if (StringUtils.hasText(nickname)) {
            return savedUser.getDisplayName();
        }
        return savedUser.getName();
    }

    private User createUserWithNicknameTagRetry(
            SignupSession signupSession,
            String nickname,
            Gender gender,
            LocalDate birthday
    ) {
        for (int attempt = 0; attempt < NicknameService.NICKNAME_TAG_RETRY_COUNT; attempt++) {
            String nicknameTag = nicknameService.generateNicknameTag(nickname);
            try {
                return userNicknameTagTransactionService.createSocialUserWithNicknameTag(
                        signupSession,
                        nickname,
                        nickname,
                        nicknameTag,
                        gender,
                        birthday
                );
            } catch (DataIntegrityViolationException exception) {
                if (!isNicknameTagConstraintViolation(exception)) {
                    throw exception;
                }
            }
        }
        throw new UserException(ErrorCode.NICKNAME_TAG_GENERATION_FAILED);
    }

    private void issueTokens(User savedUser, HttpServletResponse response) {
        String access = jwtUtil.createJwt("access", savedUser.getId(), savedUser.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String refresh = jwtUtil.createJwt("refresh", savedUser.getId(), savedUser.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());

        refreshTokenRepository.saveRefreshToken(savedUser.getId(), refresh, jwtConfig.getRefreshTokenValidityInSeconds());

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

    /**
     * 로컬 환경이면 로컬주소, 배포환경이면 배포환경의 주소로 리다이렉트 합니다.
     * */
    private String resolveRedirectBase(HttpServletRequest request, String env) {
        if (StringUtils.hasText(env)) {
            if ("local".equalsIgnoreCase(env)) {
                return "http://localhost:5173";
            }
            if ("preview".equalsIgnoreCase(env)) {
                return "https://preview.houme.kr";
            }
            if ("dev".equalsIgnoreCase(env)) {
                return "https://www.houme.kr";
            }
        }
        return resolveRedirectBase(request);
    }
}
