package or.sopt.houme.domain.house.repository.taste.taste;

import or.sopt.houme.domain.house.model.taste.entity.Taste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TasteRepository extends JpaRepository<Taste, Long>, TasteCustomRepository {

    Optional<Taste> findByFilename(String filename);
}
