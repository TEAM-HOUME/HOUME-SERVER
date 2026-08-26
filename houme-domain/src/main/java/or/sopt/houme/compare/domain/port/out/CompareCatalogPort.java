package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.CompareCatalogItem;

import java.util.Optional;

public interface CompareCatalogPort {
    CompareCatalogItem upsert(CompareCatalogItem item);
    Optional<CompareCatalogItem> findById(Long id);
}
