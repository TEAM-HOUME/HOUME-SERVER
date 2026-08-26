package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.compare.infra.entity.CompareCatalogItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompareCatalogItemRepository extends JpaRepository<CompareCatalogItemEntity, Long> {
    Optional<CompareCatalogItemEntity> findByEbayItemId(String ebayItemId);
}
