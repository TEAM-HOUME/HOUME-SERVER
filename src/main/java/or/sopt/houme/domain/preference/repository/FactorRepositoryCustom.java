package or.sopt.houme.domain.preference.repository;

import or.sopt.houme.domain.preference.entity.Factor;
import or.sopt.houme.domain.user.entity.User;

import java.util.List;

public interface FactorRepositoryCustom {
    List<Factor> findFactorsByIsLike(boolean isLike);
}
