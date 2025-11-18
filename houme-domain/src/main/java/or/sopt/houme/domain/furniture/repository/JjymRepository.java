package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.entity.Jjym;
import org.springframework.data.jpa.repository.JpaRepository;


public interface JjymRepository extends JpaRepository<Jjym, Long>, JjymRepositoryCustom {
}
