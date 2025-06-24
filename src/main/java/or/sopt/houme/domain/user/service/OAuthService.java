package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.client.KaKaoUserInfoClient;
import or.sopt.houme.domain.user.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.domain.user.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.domain.user.entity.SocialType;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.config.KaKaoConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final KaKaoOAuthClient kaKaoOAuthClient;
    private final KaKaoUserInfoClient kaKaoUserInfoClient;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    private final KaKaoConfig kaKaoConfig;



    public String requestRedirect() {
        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                kaKaoConfig.getClientId(), kaKaoConfig.getRedirectUri()
        );
    }


    public void kakaoLogin(String accessCode, HttpServletResponse response) {

        KaKaoOAuthTokenDTO authorizationCode = getKaKaoOAuthTokenDTO(accessCode);

        KaKaoUserInfoResponse userInfo = kaKaoUserInfoClient.getUserInfo(
                "Bearer "+authorizationCode.getAccess_token());

        Boolean userExist = userRepository.existsByEmail(userInfo.getKakao_account().getEmail());

        if (userExist == Boolean.FALSE) {
            User newUser = User.builder()
                    .name(userInfo.getProperties().getNickname())
                    .password(bCryptPasswordEncoder.encode("kakaoPassword"))
                    .email(userInfo.getKakao_account().getEmail())
                    .role(Role.ROLE_USER)
                    .socialType(SocialType.KAKAO)
                    .build();

            userRepository.save(newUser);
        }

        User byEmail = userRepository.findByEmail(userInfo.getKakao_account().getEmail());


        String access = jwtUtil.createJwt("access", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        response.setHeader("access-token", access);
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
