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


    // 회원의 식별자를 파싱하는 메서드
    @Override
    public Long getId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);
    }


    // 회원의 권한을 파싱하는 메서드
    @Override
    public String getRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }


    // 회원의 토큰 만료기간을 파싱하는 메서드
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


    // 액세스 토큰과 리프레시 토큰을 구별하는 메서드
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
     * 토큰을 생성하는 메서드
     *
     * @param category: 리프레시 토큰과 액세스 토큰을 구분하기 위한 파라미터
     *
     * @param expiredMs: 두 토큰에 따라 만료기간이 상이하기 때문에 파라미터로 받아야함
     *                   JWT는 Date 타입으로 만료기간을 저장하고 Date는 기본적으로 ms단위이기 때문에
     *                   *1000L를 해야 초단위 계산이 가능해진다
     *
     * @param id: claim에 들어가서 회원을 식별해줄 수 있도록 하는 데이터
     *          일반적으로 unique의 속성을 지녀야하고,
     *          개인적으로는 id 하나만 이용하기 보다는 두 개정도 이용하면 좋지 않을까 하는 생각이 들긴하지만..
     * */
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
