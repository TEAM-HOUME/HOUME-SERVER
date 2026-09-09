package or.sopt.houme.priceCompare.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 외부 상품 페이지에서 추출한 메타데이터.
 * JSON-LD·OG·사이트별 파서 중 무엇이 뽑았든 결과는 이 타입 하나로 표현한다.
 *
 * <p>파서 우선순위는 "덮어쓰기"가 아니라 "빈 칸 채우기"로 동작한다 —
 * 1순위가 상품명만 뽑고 가격을 놓쳐도 2순위가 가격만 보충할 수 있어야
 * 파서 하나의 실패가 결과 전체를 날리지 않는다. 그 규칙을 {@link #fillMissingFrom} 이 소유한다.
 */
public record ScrapedProduct(
        String sourceUrl,
        String title,
        String thumbnailUrl,
        String brand,
        Long price,
        String currency,
        List<String> additionalImageUrls,
        String description
) {

    private static final int MAX_ADDITIONAL_IMAGES = 5;

    public ScrapedProduct {
        additionalImageUrls = additionalImageUrls == null ? List.of() : List.copyOf(additionalImageUrls);
    }

    public static ScrapedProduct empty(String sourceUrl) {
        return new ScrapedProduct(sourceUrl, null, null, null, null, null, List.of(), null);
    }

    /**
     * 이미 채워진 값은 그대로 두고, 비어 있는 칸만 {@code other} 의 값으로 보충한다.
     * 추가 이미지는 순서를 지키며 합치되 중복을 제거하고 상한을 넘기지 않는다.
     */
    public ScrapedProduct fillMissingFrom(ScrapedProduct other) {
        if (other == null) {
            return this;
        }
        return new ScrapedProduct(
                sourceUrl,
                firstPresent(title, other.title()),
                firstPresent(thumbnailUrl, other.thumbnailUrl()),
                firstPresent(brand, other.brand()),
                price != null ? price : other.price(),
                firstPresent(currency, other.currency()),
                mergeImages(additionalImageUrls, other.additionalImageUrls()),
                firstPresent(description, other.description())
        );
    }

    /**
     * 검색 파이프라인을 태울 수 있는 최소 입력을 확보했는지.
     * 상품명·이미지 중 하나라도 있으면 키워드 검색이든 이미지 검색이든 시도할 수 있다.
     */
    public boolean hasEssentials() {
        return isPresent(title) || isPresent(thumbnailUrl);
    }

    public ScrapeQuality quality() {
        if (isPresent(title) && isPresent(thumbnailUrl) && isPresent(brand) && price != null) {
            return ScrapeQuality.FULL;
        }
        if (isPresent(title) && isPresent(thumbnailUrl)) {
            return ScrapeQuality.PARTIAL;
        }
        return ScrapeQuality.MINIMAL;
    }

    private static String firstPresent(String current, String candidate) {
        return isPresent(current) ? current : (isPresent(candidate) ? candidate : null);
    }

    private static List<String> mergeImages(List<String> current, List<String> candidate) {
        LinkedHashSet<String> merged = new LinkedHashSet<>(current);
        if (candidate != null) {
            merged.addAll(candidate);
        }
        List<String> limited = new ArrayList<>(merged);
        return limited.size() <= MAX_ADDITIONAL_IMAGES
                ? List.copyOf(limited)
                : List.copyOf(limited.subList(0, MAX_ADDITIONAL_IMAGES));
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
