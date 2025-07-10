package or.sopt.houme.domain.taste.repository;

import or.sopt.houme.domain.taste.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {
}
