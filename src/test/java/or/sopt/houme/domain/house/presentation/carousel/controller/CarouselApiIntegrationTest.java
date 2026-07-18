package or.sopt.houme.domain.house.presentation.carousel.controller;

import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 캐러셀 조회 API 계약 특성화 테스트 — 클러스터(carousel) 헥사고날 전환(#582) 안전망.
 *
 * <p>{@code GET /api/v1/carousels} 가 인증 하에 ApiResponse 포맷으로 200을 반환하는 계약을 고정한다.
 * (데이터 미시드 시 빈 목록이라도 엔드포인트/엔벨로프/페이지네이션 파라미터 계약 유지 검증)
 */
@Transactional
class CarouselApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Test
    @DisplayName("GET /api/v1/carousels 는 인증 하에 ApiResponse 포맷으로 200을 반환한다")
    void getCarousels_returnsApiResponse() throws Exception {
        User user = userRepository.saveAndFlush(User.builder()
                .email("carousel@houme.com").nickname("캐러셀").nicknameTag("9001").name("캐러셀")
                .role(Role.ROLE_USER).hasGeneratedImage(false).build());
        String token = jwtUtil.createJwt("access", user.getId(), Role.ROLE_USER.name(), 86_400_000L);

        mockMvc.perform(get("/api/v1/carousels").param("page", "0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data").exists());
    }
}
