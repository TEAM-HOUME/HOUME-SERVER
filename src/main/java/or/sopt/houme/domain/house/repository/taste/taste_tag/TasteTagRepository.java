package or.sopt.houme.domain.house.repository.taste.taste_tag;

import or.sopt.houme.domain.house.model.taste.entity.Taste;
import or.sopt.houme.domain.house.model.taste.entity.TasteTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TasteTagRepository extends JpaRepository<TasteTag, Long>, TasteTagCustomRepository {

    List<TasteTag> findAllByTaste(Taste taste);
}
