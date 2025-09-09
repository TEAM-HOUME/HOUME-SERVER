package or.sopt.houme.domain.taste.repository.tag;

import or.sopt.houme.domain.taste.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {
    Optional<Tag> findByTagNameKr(String tagNameKr);

    List<Tag> findAllByOrderByPriorityAsc();
}
