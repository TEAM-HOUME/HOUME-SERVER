package or.sopt.houme.domain.user.presentation.controller;

import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetailsService;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageGeneratedImageV2Response;
import or.sopt.houme.domain.user.presentation.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.model.entity.*;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.service.OAuthService;
import or.sopt.houme.domain.user.service.UserDeletionService;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {UserController.class, UserV2Controller.class},
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDeletionService userDeletionService;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private JWTConfig jwtConfig;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private BlacklistTokenRepository blacklistTokenRepository;

    private CustomUserDetails mockUserDetails;
    private MyPageInfoResponse mockResponse;

    @BeforeEach
    void setUp() {
        User mockUser = User.builder()
                .id(1L)
                .name("테스트유저")
                .birthday(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .email("test@example.com")
                .password("encodedPassword")
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();

        mockUserDetails = new CustomUserDetails(mockUser);
        mockResponse = MyPageInfoResponse.of(1L, "테스트유저", 10L);
    }

    @Test
    @DisplayName("마이페이지 유저 정보 조회 성공")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getMyPageInfo_Success() throws Exception {
        // Given
        Long id = 1L;
        User mockUser = mockUserDetails.getUser();

        // 정확히 동일한 객체를 넘겨서 mock 동작 유도
        given(customUserDetailsService.loadUserById(id)).willReturn(mockUserDetails);
        given(userService.getMyPageInfo(mockUser)).willReturn(mockResponse);

        // SecurityContext에 수동 주입
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When & Then
        mockMvc.perform(get("/api/v1/mypage/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"));
    }

    @Test
    @DisplayName("마이페이지 생성 이미지 이력 v2 조회 성공")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getMyPageInfoV2_Success() throws Exception {
        Long id = 1L;
        User mockUser = mockUserDetails.getUser();

        given(customUserDetailsService.loadUserById(id)).willReturn(mockUserDetails);
        given(userService.getUserGeneratedImageHistoryListV2(mockUser))
                .willReturn(MyPageGeneratedImageV2Response.of(List.of()));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/v2/mypage/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"));
    }

    @Test
    @DisplayName("POST /api/v1/sign-up 요청 시 소셜 회원가입이 처리된다")
    void socialSignUp_Success() throws Exception {
        given(oAuthService.signUpWithToken(
                any(String.class),
                any(String.class),
                any(Gender.class),
                any(LocalDate.class),
                any(HttpServletResponse.class)
        )).willReturn("테스트유저");

        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "signupToken": "signup-token",
                                  "name": "테스트유저",
                                  "gender": "MALE",
                                  "birthday": "2000-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("테스트유저"));
    }
}
