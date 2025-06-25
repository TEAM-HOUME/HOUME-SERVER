package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.user.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    Boolean existsByRefreshToken(String refreshToken);

    void deleteByRefreshToken(String refreshToken);
}