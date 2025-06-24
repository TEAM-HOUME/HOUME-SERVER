package or.sopt.houme.global.jwt;

public interface JWTUtil {
    Long getId(String token);

    String getRole(String token);

    Boolean isExpired(String token);

    String getCategory(String token);

    String createJwt(String category, Long id, String role, Long expiredMs);
}
