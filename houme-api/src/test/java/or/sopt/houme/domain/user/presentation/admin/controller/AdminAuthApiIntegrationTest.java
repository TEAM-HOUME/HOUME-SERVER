package or.sopt.houme.domain.user.presentation.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.domain.user.model.admin.entity.Admin;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.domain.user.repository.admin.AdminRepository;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.BCrypt;
import or.sopt.houme.support.IntegrationTestSupport;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 백오피스 클라이언트가 어드민 API를 쓰기 위한 인증·인가 계약 (#696).
 *
 * <p>- 어드민 로그인은 JSON으로 액세스 토큰을 돌려준다.
 * <p>- 어드민 토큰은 admins 테이블 기준으로 인증되고, 어드민 API(/api/v1/admin/**, /api/admin/**)는 어드민 토큰만 통과한다.
 * <p>- 어드민 토큰으로는 회원 API를 호출할 수 없다 (기획 의사결정용 for-plan 조회는 예외).
 */
@Transactional
class AdminAuthApiIntegrationTest extends IntegrationTestSupport {

    private static final String ADMIN_USERNAME = "backoffice@houme.com";
    private static final String ADMIN_PASSWORD = "admin-password";

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCrypt bCrypt;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Admin seedAdmin() {
        return adminRepository.saveAndFlush(Admin.builder()
                .username(ADMIN_USERNAME)
                .password(bCrypt.hash(ADMIN_PASSWORD))
                .role(Role.ROLE_ADMIN)
                .build());
    }

    private UserJpaEntity seedUser() {
        return userRepository.saveAndFlush(UserJpaEntity.builder()
                .email("member@houme.com")
                .nickname("회원")
                .nicknameTag("0001")
                .name("회원")
                .role(Role.ROLE_USER)
                .hasGeneratedImage(false)
                .build());
    }

    private String adminBearer(Long adminId) {
        return "Bearer " + jwtUtil.createAdminAccessToken(adminId, 3600L);
    }

    private String userBearer(Long userId) {
        return "Bearer " + jwtUtil.createJwt("access", userId, Role.ROLE_USER.name(), 3600L);
    }

    private String loginBody(String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of("email", ADMIN_USERNAME, "password", password));
    }

    @Test
    @DisplayName("어드민 로그인 API는 액세스 토큰을 JSON으로 반환하고, 그 토큰으로 어드민 API를 호출할 수 있다")
    void login_returnsAccessTokenUsableForAdminApi() throws Exception {
        seedAdmin();

        String body = mockMvc.perform(post("/api/v1/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.expiresIn").value(86400))
                .andReturn().getResponse().getContentAsString();

        JsonNode data = objectMapper.readTree(body).get("data");
        String accessToken = data.get("accessToken").asText();
        assertThat(jwtUtil.isAdminToken(accessToken)).isTrue();

        mockMvc.perform(get("/api/v1/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("어드민 로그인 API는 비밀번호가 틀리면 40401을 반환한다")
    void login_wrongPassword() throws Exception {
        seedAdmin();

        mockMvc.perform(post("/api/v1/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("wrong-password")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    @DisplayName("회원 토큰이나 토큰 없이 /api/v1/admin/** 을 호출하면 403")
    void adminApi_rejectsUserTokenAndAnonymous() throws Exception {
        UserJpaEntity member = seedUser();

        mockMvc.perform(get("/api/v1/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, userBearer(member.getId())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("어드민 토큰으로 /api/admin/** (가격비교 프리셋) 을 호출할 수 있다")
    void compareAdminApi_allowsAdminToken() throws Exception {
        Admin admin = seedAdmin();

        mockMvc.perform(get("/api/admin/v1/compare/presets")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer(admin.getId())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("어드민 토큰으로 회원 API를 호출하면 다른 회원으로 인증되지 않고 403")
    void userApi_rejectsAdminToken() throws Exception {
        Admin admin = seedAdmin();

        mockMvc.perform(get("/api/v1/mypage/user")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer(admin.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("삭제된(존재하지 않는) 어드민의 토큰은 401(40104)")
    void adminApi_rejectsTokenOfMissingAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer(999_999L)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40104));
    }

    @Test
    @DisplayName("#696 이전 형식의 어드민 토큰(principalType 없음, role=ROLE_ADMIN)은 id가 같은 회원으로 인증되지 않고 401(40103)")
    void legacyAdminToken_isRejected() throws Exception {
        UserJpaEntity member = seedUser();
        // 옛 어드민 로그인은 createJwt("access", adminId, "ROLE_ADMIN", ...) 로 발급했다 — id가 회원 id와 겹치는 상황을 재현
        String legacyAdminBearer = "Bearer " + jwtUtil.createJwt("access", member.getId(), Role.ROLE_ADMIN.name(), 3600L);

        mockMvc.perform(get("/api/v1/mypage/user")
                        .header(HttpHeaders.AUTHORIZATION, legacyAdminBearer))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40103));

        mockMvc.perform(get("/api/v1/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, legacyAdminBearer))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40103));

        // 로그인 API는 화이트리스트라 옛 토큰을 헤더에 실은 채 호출해도 다시 로그인할 수 있다
        seedAdmin();
        mockMvc.perform(post("/api/v1/admin/login")
                        .header(HttpHeaders.AUTHORIZATION, legacyAdminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(ADMIN_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 토큰은 옛 어드민 토큰 차단 로직의 영향을 받지 않는다")
    void userToken_isNotAffectedByLegacyAdminCheck() throws Exception {
        UserJpaEntity member = seedUser();

        mockMvc.perform(get("/api/v1/mypage/user")
                        .header(HttpHeaders.AUTHORIZATION, userBearer(member.getId())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("기획 의사결정용 for-plan 조회는 어드민 토큰·회원 토큰 모두 인가를 통과한다")
    void forPlan_allowsAdminAndUserToken() throws Exception {
        Admin admin = seedAdmin();
        UserJpaEntity member = seedUser();
        String forPlanUrl = "/api/v1/generated-images/{tagId}/curations/products/{furnitureId}/for-plan";

        // 인가를 통과해 서비스까지 도달하면 존재하지 않는 태그라 40406이 난다 (403이나 500이 아님)
        mockMvc.perform(get(forPlanUrl, 999_999L, 999_999L)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer(admin.getId()))
                        .param("searchKeyword", "소파")
                        .param("pHash", "50"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40406));

        mockMvc.perform(get(forPlanUrl, 999_999L, 999_999L)
                        .header(HttpHeaders.AUTHORIZATION, userBearer(member.getId()))
                        .param("searchKeyword", "소파")
                        .param("pHash", "50"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40406));
    }

    @Test
    @DisplayName("백오피스 로컬 개발 origin(localhost:5175)의 CORS 사전 요청을 허용한다")
    void cors_allowsBackofficeLocalOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/admin/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5175")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5175"));
    }
}
