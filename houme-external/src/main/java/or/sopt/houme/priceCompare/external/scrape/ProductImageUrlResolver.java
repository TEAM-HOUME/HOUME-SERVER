package or.sopt.houme.priceCompare.external.scrape;

import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * HTML에 적힌 이미지 주소를 그대로 쓸 수 있는 절대 URL로 바꾼다.
 *
 * <p>실제 페이지의 이미지 주소는 바로 쓸 수 있는 상태가 아니다 —
 * 도메인이 빠진 상대경로(`/img/a.jpg`), 스킴이 빠진 형태(`//cdn/a.jpg`),
 * 해상도별로 나열된 srcset, 로딩 전까지 1x1 투명 이미지를 넣어두고 실제 주소는
 * `data-src` 에 숨겨두는 lazy-load 까지 섞여 있다. 그 처리를 여기에 모은다.
 */
@Component
public class ProductImageUrlResolver {

    /** lazy-load 몰이 실제 주소를 숨겨두는 속성들. 앞에서부터 우선 확인한다. */
    private static final List<String> IMAGE_ATTRIBUTES =
            List.of("src", "data-src", "data-original", "data-lazy", "data-echo");

    /** 상품 이미지가 아닌 것이 거의 확실한 주소 패턴. */
    private static final List<String> PLACEHOLDER_MARKERS =
            List.of("placeholder", "blank.", "spacer", "1x1", "noimage", "no_image", "no-image", "dummy");

    /**
     * {@code <img>} 엘리먼트에서 가장 쓸만한 이미지 주소를 고른다.
     * srcset 이 있으면 그중 가장 큰 해상도를 우선한다.
     */
    public Optional<String> resolveFromElement(Element image, String baseUri) {
        Optional<String> fromSrcset = largestFromSrcset(image.attr("srcset"), baseUri);
        if (fromSrcset.isPresent()) {
            return fromSrcset;
        }
        return IMAGE_ATTRIBUTES.stream()
                .map(image::attr)
                .flatMap(raw -> resolve(raw, baseUri).stream())
                .findFirst();
    }

    /**
     * 원시 문자열 하나를 절대 URL로 만든다.
     * 데이터 URI·플레이스홀더로 판단되면 비어 있는 값을 돌려준다.
     */
    public Optional<String> resolve(String rawUrl, String baseUri) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return Optional.empty();
        }
        String candidate = rawUrl.trim();
        if (candidate.toLowerCase(Locale.ROOT).startsWith("data:")) {
            return Optional.empty();
        }
        if (candidate.startsWith("//")) {
            candidate = "https:" + candidate;
        }

        String absolute = toAbsolute(candidate, baseUri);
        if (absolute == null || isPlaceholder(absolute)) {
            return Optional.empty();
        }
        return Optional.of(absolute);
    }

    /**
     * {@code "a.jpg 1x, a@2x.jpg 2x"} 형태에서 디스크립터가 가장 큰 후보를 고른다.
     * 디스크립터가 없으면 나열 순서상 마지막(관례상 가장 큰 이미지)을 쓴다.
     */
    private Optional<String> largestFromSrcset(String srcset, String baseUri) {
        if (srcset == null || srcset.isBlank()) {
            return Optional.empty();
        }

        String best = null;
        double bestWeight = -1;
        for (String candidate : srcset.split(",")) {
            String[] parts = candidate.trim().split("\\s+");
            if (parts.length == 0 || parts[0].isBlank()) {
                continue;
            }
            double weight = parts.length > 1 ? parseDescriptor(parts[1]) : 0;
            if (weight >= bestWeight) {
                bestWeight = weight;
                best = parts[0];
            }
        }
        return resolve(best, baseUri);
    }

    private double parseDescriptor(String descriptor) {
        try {
            return Double.parseDouble(descriptor.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 상대 주소를 base 에 붙여 절대 URL로 만든다.
     *
     * <p>공백은 먼저 퍼센트 인코딩한다 — `/images/sofa main.jpg` 처럼 파일명에 공백이 든 몰이 실제로 있고,
     * {@link URI} 는 공백을 불법 문자로 보고 예외를 던져 정상 이미지가 버려진다.
     */
    private String toAbsolute(String candidate, String baseUri) {
        try {
            URI resolved = new URI(baseUri).resolve(candidate.replace(" ", "%20"));
            return resolved.isAbsolute() ? resolved.toString() : null;
        } catch (URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isPlaceholder(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return PLACEHOLDER_MARKERS.stream().anyMatch(lower::contains);
    }
}
