package or.sopt.houme.domain.user.presentation.controller.dto;

import java.security.Principal;

/**
 * 어드민 토큰으로 인증된 요청의 principal.
 *
 * <p>회원 principal({@link CustomUserDetails})과 타입이 다르므로,
 * 어드민 토큰이 회원 API에 들어와도 id가 같은 다른 회원으로 인증되지 않는다.
 */
public record AdminPrincipal(Long adminId) implements Principal {

    @Override
    public String getName() {
        return "admin:" + adminId;
    }
}
