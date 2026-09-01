package or.sopt.houme.priceCompare.external.scrape;

import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import org.jsoup.nodes.Document;

import java.util.Optional;

/**
 * 상품 페이지 HTML에서 메타데이터를 뽑는 전략.
 *
 * <p>쇼핑몰마다 HTML 구조가 달라 단일 파서로는 커버가 안 된다.
 * 구현체를 {@link #order()} 순으로 세워두고 앞선 파서가 놓친 필드만 뒤에서 채운다.
 */
public interface ProductPageParser {

    /** 뽑아낸 값이 하나도 없으면 {@link Optional#empty()}. */
    Optional<ScrapedProduct> parse(Document document, String sourceUrl);

    /** 낮을수록 먼저 시도한다. */
    int order();
}
