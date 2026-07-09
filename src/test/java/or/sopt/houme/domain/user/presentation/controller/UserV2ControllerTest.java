package or.sopt.houme.domain.user.presentation.controller;

import jakarta.servlet.http.HttpServletResponse;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetailsService;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageProfileResponse;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.service.NicknameService;
import or.sopt.houme.domain.user.service.OAuthService;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserV2Controller.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private NicknameService nicknameService;

    @MockBean
    private JWTConfig jwtConfig;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private BlacklistTokenRepository blacklistTokenRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("PATCH /api/v2/sign-up 요청 시 자체 회원가입 v2가 처리된다")
    void selfSignUpV2_success() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();

        UsernamePasswordAuthenticationToken requestAuthentication =
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user),
                        null,
                        List.of(() -> "ROLE_USER")
                );

        given(userService.updateUserV2(
                any(),
                any(String.class),
                any(Gender.class),
                any(LocalDate.class)
        )).willReturn("느긋한펭귄");

        mockMvc.perform(patch("/api/v2/sign-up")
                        .with(authentication(requestAuthentication))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "느긋한펭귄",
                                  "gender": "MALE",
                                  "birthday": "2000-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("느긋한펭귄"));
    }

    @Test
    @DisplayName("POST /api/v2/sign-up 요청 시 소셜 회원가입이 처리된다")
    void socialSignUpV2_success() throws Exception {
        given(oAuthService.signUpWithTokenV2(
                any(String.class),
                any(String.class),
                any(Gender.class),
                any(LocalDate.class),
                any(HttpServletResponse.class)
        )).willReturn("잠자는고양이1470");

        mockMvc.perform(post("/api/v2/sign-up")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "signupToken": "signup-token",
                                  "nickname": "잠자는고양이1470",
                                  "gender": "MALE",
                                  "birthday": "2000-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("잠자는고양이1470"));
    }

    @Test
    @DisplayName("GET /api/v2/nickname/rotate 요청 시 랜덤 닉네임을 반환한다")
    void rotateNickname_success() throws Exception {
        given(nicknameService.rotateNickname()).willReturn("느긋한펭귄");

        mockMvc.perform(get("/api/v2/nickname/rotate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("느긋한펭귄"));
    }

    @Test
    @DisplayName("GET /api/v2/mypage/user 요청 시 마이페이지 프로필을 조회한다")
    void getMyPageProfile_success() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();

        UsernamePasswordAuthenticationToken requestAuthentication =
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user),
                        null,
                        List.of(() -> "ROLE_USER")
                );

        given(userService.getMyPageProfile(any()))
                .willReturn(new MyPageProfileResponse(
                        1L,
                        "잠자는꾸민성1470",
                        LocalDate.of(2001, 1, 1),
                        Gender.FEMALE
                ));

        mockMvc.perform(get("/api/v2/mypage/user")
                        .with(authentication(requestAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("잠자는꾸민성1470"))
                .andExpect(jsonPath("$.data.birthday").value("2001-01-01"))
                .andExpect(jsonPath("$.data.gender").value("FEMALE"));
    }

    @Test
    @DisplayName("PATCH /api/v2/mypage/user 요청 시 마이페이지 프로필을 수정한다")
    void updateMyPageProfile_success() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();

        UsernamePasswordAuthenticationToken requestAuthentication =
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user),
                        null,
                        List.of(() -> "ROLE_USER")
                );

        given(userService.updateMyPageProfile(
                any(),
                any(String.class),
                any(Gender.class),
                any(LocalDate.class)
        )).willReturn(new MyPageProfileResponse(
                1L,
                "잠자는꾸민성1470",
                LocalDate.of(2001, 1, 1),
                Gender.FEMALE
        ));

        mockMvc.perform(patch("/api/v2/mypage/user")
                        .with(authentication(requestAuthentication))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "잠자는꾸민성1470",
                                  "gender": "FEMALE",
                                  "birthday": "2001-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("잠자는꾸민성1470"))
                .andExpect(jsonPath("$.data.birthday").value("2001-01-01"))
                .andExpect(jsonPath("$.data.gender").value("FEMALE"));
    }

    @Test
    @DisplayName("PATCH /api/v2/mypage/user 요청은 전달된 필드만 수정할 수 있다")
    void updateMyPageProfile_partialSuccess() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();

        UsernamePasswordAuthenticationToken requestAuthentication =
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user),
                        null,
                        List.of(() -> "ROLE_USER")
                );

        given(userService.updateMyPageProfile(
                any(),
                any(String.class),
                any(),
                any()
        )).willReturn(new MyPageProfileResponse(
                1L,
                "새닉네임",
                LocalDate.of(2001, 1, 1),
                Gender.FEMALE
        ));

        mockMvc.perform(patch("/api/v2/mypage/user")
                        .with(authentication(requestAuthentication))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "새닉네임"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }
}
