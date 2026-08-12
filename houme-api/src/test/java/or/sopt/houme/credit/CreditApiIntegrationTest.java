package or.sopt.houme.credit;

import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 크레딧 API 계약(정합성) 특성화 테스트 — 헥사고날 리팩터링(#581) 전/후로 동일하게 그린이어야 하는 안전망.
 *
 * <p>실제 PostgreSQL/Redis 위에서 HTTP 엔드포인트를 관통해 크레딧 지급/잔액조회 동작을 고정한다.
 * 크레딧 적립도 리포지토리가 아니라 <b>어드민 지급 API</b>로 수행하므로, 내부 구현(엔티티/서비스)이
 * 바뀌어도 이 테스트는 그대로 유지된다.
 */
@Transactional
class CreditApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private UserJpaEntity seedUser(String email, String nickname, String tag) {
        return userRepository.saveAndFlush(UserJpaEntity.builder()
                .email(email)
                .nickname(nickname)
                .nicknameTag(tag)
                .name(nickname)
                .role(Role.ROLE_USER)
                .hasGeneratedImage(false)
                .build());
    }

    private String tokenFor(Long userId, Role role) {
        return jwtUtil.createJwt("access", userId, role.name(), 86_400_000L);
    }

    private String bearer(Long userId, Role role) {
        return "Bearer " + tokenFor(userId, role);
    }

    @Test
    @DisplayName("어드민 크레딧 지급 API는 지급 개수만큼 잔액을 늘리고 잔액을 반환한다 (누적)")
    void grantCredits_accumulatesBalance() throws Exception {
        UserJpaEntity member = seedUser("grant@houme.com", "지급대상", "0001");
        String auth = bearer(member.getId(), Role.ROLE_ADMIN);

        // 최초 5개 지급 → 잔액 5
        mockMvc.perform(post("/api/v1/admin/members/{id}/credits", member.getId())
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AmountBody(5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.memberId").value(member.getId()))
                .andExpect(jsonPath("$.data.grantedAmount").value(5))
                .andExpect(jsonPath("$.data.creditBalance").value(5));

        // 추가 3개 지급 → 잔액 8 (누적)
        mockMvc.perform(post("/api/v1/admin/members/{id}/credits", member.getId())
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AmountBody(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.creditBalance").value(8));
    }

    @Test
    @DisplayName("어드민 회원 검색 API는 회원의 현재 ACTIVE 크레딧 잔액을 반환한다")
    void searchMembers_returnsCreditBalance() throws Exception {
        UserJpaEntity member = seedUser("search@houme.com", "검색대상", "0002");
        String auth = bearer(member.getId(), Role.ROLE_ADMIN);

        mockMvc.perform(post("/api/v1/admin/members/{id}/credits", member.getId())
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AmountBody(4))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/members")
                        .header("Authorization", auth)
                        .param("keyword", "search@houme.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.members[0].memberId").value(member.getId()))
                .andExpect(jsonPath("$.data.members[0].email").value("search@houme.com"))
                .andExpect(jsonPath("$.data.members[0].creditBalance").value(4));
    }

    @Test
    @DisplayName("마이페이지 API는 사용 가능한 크레딧 개수를 반환한다")
    void myPage_returnsCreditCount() throws Exception {
        UserJpaEntity member = seedUser("mypage@houme.com", "마이페이지", "0003");

        // 어드민 지급으로 2개 적립
        mockMvc.perform(post("/api/v1/admin/members/{id}/credits", member.getId())
                        .header("Authorization", bearer(member.getId(), Role.ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AmountBody(2))))
                .andExpect(status().isOk());

        // 본인 토큰으로 마이페이지 조회 → creditCount 2
        mockMvc.perform(get("/api/v1/mypage/user")
                        .header("Authorization", bearer(member.getId(), Role.ROLE_USER)))
                .andExpect(status().isOk())
                // 실제 API 계약상 필드명이 대문자 C로 시작한다 (MyPageInfoResponse.CreditCount)
                .andExpect(jsonPath("$.data.CreditCount").value(2));
    }

    @Test
    @DisplayName("크레딧 지급 API는 잘못된 개수(0, 상한 초과)를 400으로 거부한다")
    void grantCredits_rejectsInvalidAmount() throws Exception {
        UserJpaEntity member = seedUser("invalid@houme.com", "검증대상", "0004");
        String auth = bearer(member.getId(), Role.ROLE_ADMIN);

        mockMvc.perform(post("/api/v1/admin/members/{id}/credits", member.getId())
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AmountBody(0))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/admin/members/{id}/credits", member.getId())
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AmountBody(5000))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 회원에게 지급하면 40401(USER_NOT_FOUND)")
    void grantCredits_memberNotFound() throws Exception {
        UserJpaEntity actor = seedUser("actor@houme.com", "액터", "0005");

        mockMvc.perform(post("/api/v1/admin/members/{id}/credits", 999_999L)
                        .header("Authorization", bearer(actor.getId(), Role.ROLE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AmountBody(1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    private record AmountBody(Integer amount) {
    }
}
