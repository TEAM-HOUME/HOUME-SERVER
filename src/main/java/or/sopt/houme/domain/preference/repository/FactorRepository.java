package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.Factor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactorRepository extends JpaRepository<Factor, Long>, FactorRepositoryCustom {
}
