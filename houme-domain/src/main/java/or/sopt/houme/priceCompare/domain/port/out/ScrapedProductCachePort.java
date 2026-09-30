package or.sopt.houme.priceCompare.domain.port.out;

import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;

import java.util.Optional;

public interface ScrapedProductCachePort {

    Optional<ScrapedProduct> find(SourceUrl sourceUrl);

    void save(ScrapedProduct product);
}
