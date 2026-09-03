package or.sopt.houme.compare.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComparePresetItemJpaRepository extends JpaRepository<ComparePresetItemJpaEntity, Long> {
    List<ComparePresetItemJpaEntity> findByPreset(ComparePresetJpaEntity preset);
}
