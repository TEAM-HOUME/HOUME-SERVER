package or.sopt.houme.user.domain.port.out;

/**
 * 어드민 계정 영속화 아웃바운드 포트.
 * JWT 필터가 어드민 토큰의 주체(admins 테이블)가 실제로 존재하는지 확인할 때 사용한다.
 * 구현은 infra 어댑터가 제공한다.
 */
public interface AdminRepositoryPort {

    boolean existsById(Long adminId);
}
