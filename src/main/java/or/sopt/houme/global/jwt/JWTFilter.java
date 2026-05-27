package or.sopt.houme.global.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetailsService;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.config.WhiteListConfig;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static or.sopt.houme.global.logging.LogMarkers.fields;


/**
 * 토큰을 검증하는 클래스 입니다
 * 토큰의 검증은 컨트롤러단이 아닌, 필터 단에서 구현되어 서블릿에 접근하기 전에 통일된 검증을 진행합니다
 *
 * 이때 OncePerRequestFilter 를 상속받아,
 * 요청의 수명주기 안에서 단 한 번만 실행되어 필요없는 검증이 추가적으로 실행되는 오버헤드를 방지합니다
 * */
@RequiredArgsConstructor
@Component
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private static final List<String> ALL_WHITELIST = Stream.of(
            WhiteListConfig.swaggerWhitelist(),
            WhiteListConfig.oauthWhitelist(),
            WhiteListConfig.serverWhitelist(),
            WhiteListConfig.makeHouseWhitelist(),
            WhiteListConfig.userWhiteList(),
            WhiteListConfig.exploreWhiteList(),
            WhiteListConfig.monitoringWhiteList(),
            WhiteListConfig.adminWhiteList(),
            WhiteListConfig.curationWhiteList()
    ).flatMap(List::stream).toList();

    private boolean isWhitelisted(String uri) {
        return ALL_WHITELIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = null;

        /**
         *
         * 2025/08/28
         * ADMIN 로그인을 위해 쿼리파라미터에서 1차적으로 토큰을 검증하는 로직을 추가하였습니다.
         *
         * 기존에는 쿠키를 이용한 검증을 추가할 것을 고려하였지만,
         * 그러면 실제 사용자가 이용할 때 리프레시 토큰이 검증되어 예외가 발생할 것을 우려하여 쿼리 파라미터로 우회하였습니다
         *
         * */
        String tokenFromParameter = request.getParameter("token");

        if (tokenFromParameter != null) {
            accessToken = tokenFromParameter;
        } else {
            // 헤더에서 Authorization 토큰을 꺼냄
            String authorizationHeader = request.getHeader(jwtConfig.getHeader());

            // Authorization 헤더가 없거나 Bearer 스킴이 없으면 다음 필터로 이동
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            // Bearer 뒤의 토큰을 추출
            accessToken = authorizationHeader.substring(7).trim();
        }

        // 토큰이 없는 경우 필터 체인 계속 진행
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        /**
         * 토큰의 유효시간을 검증하고
         * 토큰의 헤더가 access인지 확인해서 액세스토큰이 맞는지 검증한다
         * */
        try {

            jwtUtil.isExpired(accessToken);

            // 블랙리스트 검사
            String jti = jwtUtil.getJti(accessToken);
            if (blacklistTokenRepository.exists(jti)) {
                if (isWhitelisted(request.getRequestURI())) {
                    filterChain.doFilter(request, response);
                    return;
                }
                logAuthFailure(request, ErrorCode.ACCESS_TOKEN_BLACKLISTED);
                setErrorResponse(response, ErrorCode.ACCESS_TOKEN_BLACKLISTED);
                return;
            }

            String category = jwtUtil.getCategory(accessToken);
            if (!"access".equals(category)) {
                if (isWhitelisted(request.getRequestURI())) {
                    filterChain.doFilter(request, response);
                    return;
                }
                logAuthFailure(request, ErrorCode.ACCESS_INVALID_TYPE);
                setErrorResponse(response, ErrorCode.ACCESS_INVALID_TYPE);
                return;
            }

        } catch (ExpiredJwtException e) {
            if (isWhitelisted(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }
            logAuthFailure(request, ErrorCode.ACCESS_TOKEN_EXPIRED);
            setErrorResponse(response, ErrorCode.ACCESS_TOKEN_EXPIRED);
            return;
        }

        //토큰에서 id와 role 획득
        Long id = jwtUtil.getId(accessToken);
        String roleString = jwtUtil.getRole(accessToken);

        // String role을 Role enum으로 변환
        Role role;
        try {
                role = Role.valueOf(roleString);
            } catch (IllegalArgumentException e) {
                logAuthFailure(request, ErrorCode.ROLE_INVALID_TYPE);
                setErrorResponse(response, ErrorCode.ROLE_INVALID_TYPE);

                return;
            }


        /**
         * UserDetails에 회원 정보 객체 담아서 요청에 회원정보가 필요한 경우 가져다 쓴다
         *
         *  해당 객체의 생명주기는 한 요청이기 때문에 세션유지와는 차이가 존재한다
         * */
        CustomUserDetails customUserDetails = customUserDetailsService.loadUserById(id);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        MDC.put("userId", String.valueOf(id));
        try {
            log.debug(
                    fields(
                            "event", "auth.success",
                            "method", request.getMethod(),
                            "uri", request.getRequestURI(),
                            "userId", id
                    ),
                    "authentication succeeded"
            );
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("userId");
        }
    }


    /**
     * 필터단에서 발생하는 예외는 저희가 만든 예외 핸들러로는 캐치 할 수 없습니다.
     * 서블렛까지 들어가기 전에 예외가 발생하면 return 시켜버리기 때문이죠. 그래서 이렇게 리스폰스에 직접 데이터를 넣어서 반환합니다
     * */
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"code\":%d, \"message\":\"%s\"}",
                errorCode.getCode(), errorCode.getMsg()
        ));
    }

    private void logAuthFailure(HttpServletRequest request, ErrorCode errorCode) {
        log.warn(
                fields(
                        "event", "auth.failed",
                        "method", request.getMethod(),
                        "uri", request.getRequestURI(),
                        "status", errorCode.getStatus().value(),
                        "errorCode", errorCode.getCode(),
                        "reason", errorCode.name()
                ),
                "authentication failed"
        );
    }

}
