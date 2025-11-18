package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.Factor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactorRepository extends JpaRepository<Factor, Long>, FactorRepositoryCustom {

    // Factor 조회
    Optional<Factor> findById(long id);
}
