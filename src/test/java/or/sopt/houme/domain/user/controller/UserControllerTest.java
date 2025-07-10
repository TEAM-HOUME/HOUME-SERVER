package or.sopt.houme.domain.user.controller;

import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.service.UserServiceImpl;
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

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ✅ 보안 설정 무시 + TestProfile 사용
@WebMvcTest(
        controllers = UserController.class,
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
    private UserServiceImpl userService;

    @MockBean
    private JWTConfig jwtConfig;

    @MockBean
    private JWTUtil jwtUtil;

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
                .credits(new ArrayList<>())
                .houses(new ArrayList<>())
                .build();

        mockUserDetails = new CustomUserDetails(mockUser);
        mockResponse = MyPageInfoResponse.of("테스트유저", 10L);
    }

    @Test
    @DisplayName("✅ 마이페이지 유저 정보 조회 성공")
    @WithMockUser(username = "test@example.com", roles = "USER") // ✅ 요거 추가
    void getMyPageInfo_Success() throws Exception {
        // Given
        User mockUser = mockUserDetails.getUser();

        // ✅ 정확히 동일한 객체를 넘겨서 mock 동작 유도
        given(userService.getMyPageInfo(mockUser)).willReturn(mockResponse);

        // ✅ SecurityContext에 수동 주입
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When & Then
        mockMvc.perform(get("/api/v1/mypage/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"));
    }
}
