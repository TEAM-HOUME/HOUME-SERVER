package or.sopt.houme.domain.preference.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.preference.dto.response.FactorsResponse;
import or.sopt.houme.domain.preference.entity.Factor;
import or.sopt.houme.domain.preference.repository.FactorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactorServiceImpl implements FactorService {

    private final FactorRepository factorRepositoy;

    @Override
    public FactorsResponse getFactors(boolean isLike) {
        List<Factor> factors = factorRepositoy.findFactorsByIsLike(isLike);

        return FactorsResponse.from(factors);
    }
}
