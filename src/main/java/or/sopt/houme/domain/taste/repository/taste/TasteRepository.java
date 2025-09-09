package or.sopt.houme.domain.taste.repository.taste;

import or.sopt.houme.domain.taste.entity.Taste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TasteRepository extends JpaRepository<Taste, Long>, TasteCustomRepository {

    Optional<Taste> findByFilename(String filename);
}
