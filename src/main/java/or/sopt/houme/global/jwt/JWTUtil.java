package or.sopt.houme.global.jwt;

public interface JWTUtil {
    /**
 * 주어진 JWT 토큰에서 사용자 ID를 추출하여 반환합니다.
 *
 * @param token JWT 토큰 문자열
 * @return 토큰에 포함된 사용자 ID
 */
Long getId(String token);

    /**
 * 주어진 JWT 토큰에서 사용자 역할(role) 정보를 추출하여 반환합니다.
 *
 * @param token 역할 정보를 추출할 JWT 토큰
 * @return 토큰에 포함된 사용자 역할 문자열
 */
String getRole(String token);

    /**
 * 주어진 JWT 토큰이 만료되었는지 여부를 반환합니다.
 *
 * @param token 만료 여부를 확인할 JWT 토큰 문자열
 * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
 */
Boolean isExpired(String token);

    /**
 * 주어진 JWT에서 category 정보를 추출하여 반환합니다.
 *
 * @param token category 정보를 추출할 JWT 문자열
 * @return JWT에 포함된 category 값
 */
String getCategory(String token);

    /**
 * 주어진 카테고리, 사용자 ID, 역할, 만료 시간을 기반으로 JWT 토큰 문자열을 생성합니다.
 *
 * @param category JWT에 포함될 카테고리 정보
 * @param id 사용자 식별자
 * @param role 사용자 역할 정보
 * @param expiredMs 토큰 만료 시간(밀리초 단위)
 * @return 생성된 JWT 토큰 문자열
 */
String createJwt(String category, Long id, String role, Long expiredMs);
}
