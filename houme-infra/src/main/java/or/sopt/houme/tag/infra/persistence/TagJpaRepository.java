package or.sopt.houme.tag.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagJpaRepository extends JpaRepository<TagJpaEntity, Long> {

    Optional<TagJpaEntity> findByTagNameKr(String tagNameKr);

    List<TagJpaEntity> findAllByOrderByPriorityAsc();

    Optional<TagJpaEntity> findByPriority(int priority);
}
