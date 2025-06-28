package or.sopt.houme.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtilImpl implements JWTUtil {

    private final SecretKey secretKey;

    /****
     * JWT 시크릿 키를 검증하고 HS256 알고리즘으로 인코딩하여 초기화하는 생성자입니다.
     *
     * @param secret application.yml에서 주입된 JWT 시크릿 문자열
     * @throws IllegalStateException 시크릿 키가 null인 경우 발생합니다.
     */
    public JWTUtilImpl(@Value("${spring.jwt.secret}") String secret) {
        if (secret == null) {
            throw new IllegalStateException("Secret is null!");
        }

        /**
         * 우리가 yml에 설정해 놓은 JWT의 시크릿 키를 HS256을 통해 인코딩하여 사용한다
         * 이때 secret이 비어있으면(Null) 안되는데, 이걸 빌드 단계에서 검증하기 위해 생성자 주입을 통해 검증한다
         * */
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
    }


    /**
     * JWT 토큰에서 회원의 식별자(id) 값을 추출합니다.
     *
     * @param token 파싱할 JWT 토큰 문자열
     * @return 토큰에 포함된 회원의 식별자(Long)
     */
    @Override
    public Long getId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);
    }


    /**
     * JWT 토큰에서 회원의 권한("role" 클레임)을 추출하여 반환합니다.
     *
     * @param token 권한 정보를 포함한 JWT 토큰 문자열
     * @return 토큰에 포함된 회원의 권한 문자열
     */
    @Override
    public String getRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }


    /**
     * 주어진 JWT 토큰이 만료되었는지 여부를 반환합니다.
     *
     * @param token 만료 여부를 확인할 JWT 토큰 문자열
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
     */
    @Override
    public Boolean isExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.before(new Date());
    }


    /**
     * JWT 토큰에서 "category" 클레임 값을 추출하여 반환합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 토큰의 "category" 클레임 값 (예: 액세스 토큰 또는 리프레시 토큰 구분)
     */
    @Override
    public String getCategory(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("category", String.class);
    }


    /**
     * 지정된 카테고리, 회원 ID, 역할, 만료 기간을 포함하는 JWT 토큰을 생성합니다.
     *
     * @param category 토큰의 종류를 구분하는 값 (예: access, refresh)
     * @param id 회원을 식별하는 고유 ID
     * @param role 회원의 권한 정보
     * @param expiredMs 토큰의 만료 기간(초 단위)
     * @return 생성된 JWT 토큰 문자열
     */
    @Override
    public String createJwt(String category, Long id, String role, Long expiredMs) {

        expiredMs = expiredMs * 1000L;

        return Jwts.builder()
                .claim("category", category)
                .claim("id", id)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}
