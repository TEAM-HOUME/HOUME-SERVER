package or.sopt.houme.priceCompare.application;

import or.sopt.houme.priceCompare.application.dto.PriceCompareStartResponse;

public interface PriceCompareUseCase {

    PriceCompareStartResponse start(String rawUrl);
}
