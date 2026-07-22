package or.sopt.houme.tastetag.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TasteTagJpaRepository extends JpaRepository<TasteTagJpaEntity, Long> {

    List<TasteTagJpaEntity> findAllByTasteId(Long tasteId);
}
