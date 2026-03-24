package or.sopt.houme.domain.furniture.presentation.controller;

import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ItemResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.furniture.service.facade.JjymOptimisticLockFacade;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetailsService;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = JjymController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class JjymV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JjymService jjymService;

    @MockBean
    private JjymOptimisticLockFacade jjymOptimisticLockFacade;

    @MockBean
    private JWTConfig jwtConfig;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private BlacklistTokenRepository blacklistTokenRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v2/curation-raw-products/{rawProductId}/jjym 요청 시 찜 토글 결과를 반환한다")
    void toggleRawProductJjym_success() throws Exception {
        given(jjymOptimisticLockFacade.toggleRawProduct(any(), anyLong())).willReturn(true);

        mockMvc.perform(post("/api/v2/curation-raw-products/10/jjym")
                        .with(authentication(authentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.favorited").value(true));
    }

    @Test
    @DisplayName("GET /api/v2/jjyms 요청 시 원천 상품 찜 목록을 반환한다")
    void getMyRawProductJjyms_success() throws Exception {
        given(jjymService.getMyRawProductJjyms(1L)).willReturn(
                JjymV2ListResponse.of(List.of(
                        JjymV2ItemResponse.of(
                                10L,
                                true,
                                "https://image",
                                "https://site",
                                List.of("화이트"),
                                "브랜드A",
                                "소파",
                                100000L,
                                20,
                                80000L,
                                5L
                        )
                ))
        );

        mockMvc.perform(get("/api/v2/jjyms")
                        .with(authentication(authentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].rawProductId").value(10))
                .andExpect(jsonPath("$.data.items[0].isJjym").value(true))
                .andExpect(jsonPath("$.data.items[0].brandName").value("브랜드A"))
                .andExpect(jsonPath("$.data.items[0].jjymCount").value(5));
    }

    private UsernamePasswordAuthenticationToken authentication() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();

        return new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user),
                null,
                List.of(() -> "ROLE_USER")
        );
    }
}
