package or.sopt.houme.domain.user.controller;

import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.domain.user.service.JWTService;
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

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class JWTControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JWTService jwtService;

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
    @DisplayName("GET /access 요청 시 access-token이 발급되며 메시지를 응답한다")
    void testCreateAccess() throws Exception {
        // given
        doNothing().when(jwtService).createToken(Mockito.any(HttpServletResponse.class));

        // when & then
        mockMvc.perform(get("/access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("테스트용 액세스 토큰이 발급되었습니다. 헤더를 확인해주세요"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("GET /access-test 요청 시 회원 정보와 토큰을 확인한다")
    void testAccessTest() throws Exception {

        setAuthentication(testUserDetails);

        mockMvc.perform(get("/access-test")
                        .header("Authorization", "Bearer access.token.here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("액세스 토큰이 성공적으로 작동합니다"));
    }

    @Test
    @DisplayName("POST /reissue 요청 시 토큰이 재발급된다")
    void testReissue() throws Exception {
        // given
        doNothing().when(jwtService).refreshRotate(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));

        // when & then
        mockMvc.perform(post("/reissue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").value("성공적으로 재생성되었습니다"));
    }



    private void setAuthentication(CustomUserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
