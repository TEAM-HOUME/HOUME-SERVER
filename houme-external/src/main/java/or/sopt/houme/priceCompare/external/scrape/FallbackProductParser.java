package or.sopt.houme.priceCompare.external.scrape;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.priceCompare.domain.ScrapedProduct;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 규격화된 메타데이터가 하나도 없는 페이지를 위한 최후 파서.
 *
 * <p>{@code <title>} 과 본문에서 가장 그럴듯한 이미지 하나만 건진다.
 * 정확도는 낮지만, 검색 파이프라인은 상품명이나 이미지 중 하나만 있어도 시도해볼 수 있으므로
 * 아무것도 못 건지고 실패하는 것보다는 낫다.
 */
@Component
@RequiredArgsConstructor
public class FallbackProductParser implements ProductPageParser {

    private static final int ORDER = 99;
    private static final int MIN_IMAGE_DIMENSION = 200;

    private final ProductImageUrlResolver imageUrlResolver;

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public Optional<ScrapedProduct> parse(Document document, String sourceUrl) {
        String title = document.title() == null || document.title().isBlank() ? null : document.title().trim();
        String thumbnail = findLargestImage(document, sourceUrl).orElse(null);

        if (title == null && thumbnail == null) {
            return Optional.empty();
        }
        return Optional.of(new ScrapedProduct(
                sourceUrl, title, thumbnail, null, null, null, List.of(), null));
    }

    /**
     * width/height 속성이 명시된 이미지 중 가장 큰 것을 고른다.
     * 속성이 전혀 없는 페이지에서는 첫 번째 유효 이미지로 대신한다(아이콘·로고를 집을 위험은 감수).
     */
    private Optional<String> findLargestImage(Document document, String sourceUrl) {
        String best = null;
        long bestArea = -1;
        String firstValid = null;

        for (Element image : document.select("img")) {
            Optional<String> url = imageUrlResolver.resolveFromElement(image, sourceUrl);
            if (url.isEmpty()) {
                continue;
            }
            if (firstValid == null) {
                firstValid = url.get();
            }

            long width = parseDimension(image.attr("width"));
            long height = parseDimension(image.attr("height"));
            if (width < MIN_IMAGE_DIMENSION || height < MIN_IMAGE_DIMENSION) {
                continue;
            }
            long area = width * height;
            if (area > bestArea) {
                bestArea = area;
                best = url.get();
            }
        }
        return Optional.ofNullable(best != null ? best : firstValid);
    }

    private long parseDimension(String raw) {
        try {
            return Long.parseLong(raw.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
