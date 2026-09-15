package or.sopt.houme.domain.coupang.repository;

import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CoupangProductJpaRepository extends JpaRepository<CoupangProductJpaEntity, Long> {
    Optional<CoupangProductJpaEntity> findByCoupangProductId(String coupangProductId);

    @Query("""
            select product
            from CoupangProductJpaEntity product
            where product.imageUrl is not null
              and trim(product.imageUrl) <> ''
              and (product.imageEmbedding is null or trim(product.imageEmbedding) = '')
            order by product.id asc
            """)
    List<CoupangProductJpaEntity> findProductsNeedingImageEmbedding(Pageable pageable);
}
