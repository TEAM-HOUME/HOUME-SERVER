package or.sopt.houme.coupang.domain.port.out;

import or.sopt.houme.coupang.domain.CoupangProduct;

import java.util.List;
import java.util.Optional;

public interface CoupangProductPort {
    Optional<CoupangProduct> findById(Long id);
    List<CoupangProduct> findAllByIdIn(List<Long> ids);
}
