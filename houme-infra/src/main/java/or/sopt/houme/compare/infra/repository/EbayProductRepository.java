package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.compare.infra.entity.EbayProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EbayProductRepository extends JpaRepository<EbayProductJpaEntity, Long> {
    Optional<EbayProductJpaEntity> findByEbayItemId(String ebayItemId);
}
