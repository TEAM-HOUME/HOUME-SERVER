package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.user.model.entity.NicknameWord;
import or.sopt.houme.domain.user.model.entity.NicknameWordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NicknameWordRepository extends JpaRepository<NicknameWord, Long> {
    List<NicknameWord> findAllByTypeAndIsActiveTrue(NicknameWordType type);
}
