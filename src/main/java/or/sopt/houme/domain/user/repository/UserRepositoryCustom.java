package or.sopt.houme.domain.user.repository;

public interface UserRepositoryCustom {
    long countByMemberIdAndStatus(Long userId);
}
