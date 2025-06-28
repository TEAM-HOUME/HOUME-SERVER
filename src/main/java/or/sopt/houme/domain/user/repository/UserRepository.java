package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
 * 지정한 이메일을 가진 사용자가 존재하는지 여부를 반환합니다.
 *
 * @param email 확인할 사용자의 이메일 주소
 * @return 사용자가 존재하면 true, 그렇지 않으면 false
 */
Boolean existsByEmail(String email);

    /**
 * 지정된 이메일을 가진 사용자를 조회합니다.
 *
 * @param email 조회할 사용자의 이메일
 * @return 해당 이메일을 가진 사용자 엔티티, 존재하지 않으면 null 반환
 */
User findByEmail(String email);
}
