package or.sopt.houme.domain.user.controller;

import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.domain.user.service.OAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuthService oAuthService;

    private CustomUserDetails testUserDetails;

    @BeforeEach
    void initUser() {
        User user = User.builder()
                .name("테스트유저")
                .email("test" + UUID.randomUUID() + "@example.com")
                .password("encoded-password")
                .birthday(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .hasGeneratedImage(false)
                .build();

        testUserDetails = new CustomUserDetails(user);
    }

    @Test
    @DisplayName("GET /oauth/kakao 요청 시 카카오 인증서버로 리다이렉트된다")
    void testKakaoOAuthRedirect() throws Exception {
        // given
        when(oAuthService.requestRedirect(any(HttpServletRequest.class))).thenReturn("https://kauth.kakao.com/oauth");

        // when & then
        mockMvc.perform(get("/oauth/kakao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://kauth.kakao.com/oauth"));
    }

    @Test
    @DisplayName("GET /oauth/kakao/callback 요청 시 카카오 로그인 성공 여부가 반환된다")
    void testKakaoLoginCallback() throws Exception {
        // given
        when(oAuthService.kakaoLogin(eq("abc123"), any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(true);

        // when & then
        mockMvc.perform(get("/oauth/kakao/callback")
                        .param("code", "abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("POST /logout 요청 시 로그아웃이 정상 처리된다")
    @WithMockUser(username = "testUser", roles = "USER")
    void testLogout() throws Exception {
        // given
        setAuthentication(testUserDetails);
        doNothing().when(oAuthService).logout(any(CustomUserDetails.class), any(HttpServletRequest.class),any(HttpServletResponse.class));

        // when & then
        mockMvc.perform(post("/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("로그아웃이 정상적으로 처리되었습니다"));
    }

    private void setAuthentication(CustomUserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
