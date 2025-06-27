package or.sopt.houme.global.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TokenException;
import or.sopt.houme.global.config.JWTConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * 토큰을 검증하는 클래스 입니다
 * 토큰의 검증은 컨트롤러단이 아닌, 필터 단에서 구현되어 서블릿에 접근하기 전에 통일된 검증을 진행합니다
 *
 * 이때 OncePerRequestFilter 를 상속받아,
 * 요청의 수명주기 안에서 단 한 번만 실행되어 필요없는 검증이 추가적으로 실행되는 오버헤드를 방지합니다
 * */
@RequiredArgsConstructor
@Slf4j
@Component
public class JWTFilter extends OncePerRequestFilter{

    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 Authorization 토큰을 꺼냄
        String authorizationHeader = request.getHeader(jwtConfig.getHeader());

        // Authorization 헤더가 없거나 Bearer 스킴이 없으면 다음 필터로 이동
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 뒤의 토큰을 추출
        String accessToken = authorizationHeader.substring(7).trim();

        /**
         * 토큰의 유효시간을 검증하고
         * 토큰의 헤더가 access인지 확인해서 액세스토큰이 맞는지 검증한다
         * */
        try {
            log.info("토큰 있어서 검증 시작");

            jwtUtil.isExpired(accessToken);

            String category = jwtUtil.getCategory(accessToken);
            if (!"access".equals(category)) {
                throw new TokenException(ErrorCode.ACCESS_INVALID_TYPE);
            }

        } catch (ExpiredJwtException e) {
            throw new TokenException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        }

        //토큰에서 id와 role 획득
        Long id = jwtUtil.getId(accessToken);
        String roleString = jwtUtil.getRole(accessToken);

        // String role을 Role enum으로 변환
        Role role = Role.valueOf(roleString);

        User user = User.builder()
                .id(id)
                .role(role)
                .build();

        /**
         * UserDetails에 회원 정보 객체 담아서 요청에 회원정보가 필요한 경우 가져다 쓴다
         *
         *  해당 객체의 생명주기는 한 요청이기 때문에 세션유지와는 차이가 존재한다
         * */
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}