package or.sopt.houme.priceCompare.external.scrape;

import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import or.sopt.houme.priceCompare.domain.port.out.ProductPageScrapePort;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 스크래핑 아웃바운드 포트 구현.
 *
 * <p>HTML을 한 번 내려받아, 등록된 파서를 우선순위대로 돌리며 결과를 누적한다.
 * 뒤 파서가 앞 결과를 덮어쓰지 않고 <b>비어 있는 칸만</b> 채우므로
 * (1순위가 이름·가격을, 2순위가 이미지를 뽑는 식으로) 파서 하나가 실패해도 결과가 통째로 날아가지 않는다.
 */
@Slf4j
@Component
public class ProductPageScrapeAdapter implements ProductPageScrapePort {

    private final ProductPageFetcher productPageFetcher;
    private final List<ProductPageParser> parsers;

    public ProductPageScrapeAdapter(ProductPageFetcher productPageFetcher, List<ProductPageParser> parsers) {
        this.productPageFetcher = productPageFetcher;
        this.parsers = parsers.stream()
                .sorted(Comparator.comparingInt(ProductPageParser::order))
                .toList();
    }

    @Override
    public ScrapedProduct scrape(SourceUrl sourceUrl) {
        Document document = productPageFetcher.fetch(sourceUrl.value());

        ScrapedProduct accumulated = ScrapedProduct.empty(sourceUrl.value());
        for (ProductPageParser parser : parsers) {
            accumulated = accumulated.fillMissingFrom(parser.parse(document, sourceUrl.value()).orElse(null));
        }

        log.info("상품 페이지 스크래핑 완료: url={}, quality={}", sourceUrl.value(), accumulated.quality());
        return accumulated;
    }
}
