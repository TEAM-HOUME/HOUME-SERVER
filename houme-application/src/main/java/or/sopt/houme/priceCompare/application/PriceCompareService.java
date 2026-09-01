package or.sopt.houme.priceCompare.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.application.dto.PriceCompareStartResponse;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCompareService implements PriceCompareUseCase {

    private final ProductPageScrapePort productPageScrapePort;

    @Override
    public PriceCompareStartResponse start(String rawUrl) {
        SourceUrl sourceUrl = SourceUrl.normalize(rawUrl);
        ScrapedProduct product = productPageScrapePort.scrape(sourceUrl);

        if (!product.hasEssentials()) {
            log.warn("가격 비교 시작 실패 - 필수 정보 없음: url={}", sourceUrl.value());
            throw new PriceCompareException(ErrorCode.PRODUCT_METADATA_PARSE_FAILED);
        }

        String jobId = UUID.randomUUID().toString();
        log.info("가격 비교 파이프라인 시작: jobId={}, url={}, quality={}", jobId, sourceUrl.value(), product.quality());
        return PriceCompareStartResponse.of(jobId, product);
    }
}
