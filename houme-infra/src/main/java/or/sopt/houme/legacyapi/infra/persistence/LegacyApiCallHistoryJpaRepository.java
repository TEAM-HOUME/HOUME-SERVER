package or.sopt.houme.legacyapi.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LegacyApiCallHistoryJpaRepository extends JpaRepository<LegacyApiCallHistoryJpaEntity, Long> {
}
