package or.sopt.houme.domain.taste.repository.taste_tag;

import or.sopt.houme.domain.taste.entity.TasteTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TasteTagRepository extends JpaRepository<TasteTag, Long>, TasteTagCustomRepository {
}
