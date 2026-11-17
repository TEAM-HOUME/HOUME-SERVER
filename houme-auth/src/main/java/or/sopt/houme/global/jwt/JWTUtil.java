package or.sopt.houme.global.jwt;

public interface JWTUtil {
    Long getId(String token);

    String getRole(String token);

    Boolean isExpired(String token);

    String getCategory(String token);

    String getJti(String token);

    long getRemainingExpiration(String token);

    String createJwt(String category, Long id, String role, Long expiredMs);

    /**
     * 어드민 액세스 토큰 발급. id 클레임에는 admins 테이블의 id가 들어가므로,
     * 회원 토큰과 구분하기 위한 principalType=ADMIN 클레임을 함께 싣는다.
     */
    String createAdminAccessToken(Long adminId, long expiredSeconds);

    /** principalType=ADMIN 클레임이 있는 어드민 토큰인지 확인한다. */
    boolean isAdminToken(String token);
}
