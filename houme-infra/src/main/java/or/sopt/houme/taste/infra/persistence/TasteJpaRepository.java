package or.sopt.houme.taste.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TasteJpaRepository extends JpaRepository<TasteJpaEntity, Long> {

    Optional<TasteJpaEntity> findByFilename(String filename);
}
