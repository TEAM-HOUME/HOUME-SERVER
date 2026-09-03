package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.EbayProduct;

import java.util.Optional;

public interface EbayProductPort {
    EbayProduct upsert(EbayProduct item);
    Optional<EbayProduct> findById(Long id);
}
