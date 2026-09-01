package or.sopt.houme.compare.application;

import or.sopt.houme.compare.domain.EbayCandidate;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class EbayPipelineUtils {

    // SoozipCategory → eBay categoryId 매핑 (하드필터 기준)
    public static final Map<SoozipCategory, Set<String>> EBAY_CATEGORY_MAP = new EnumMap<>(Map.of(
            SoozipCategory.FURNITURE,        Set.of("3197"),
            SoozipCategory.LIGHTING,         Set.of("20697"),
            SoozipCategory.HOME_FABRIC,      Set.of("20444", "63514"),
            SoozipCategory.LIVING_GOODS,     Set.of("26677", "20667"),
            SoozipCategory.MINI_ELECTRONICS, Set.of("20667"),
            SoozipCategory.ACCESSORY,        Set.of("10033")
    ));

    public static final double USD_TO_KRW = 1350.0;
    public static final double IMAGE_WEIGHT = 0.7;
    public static final double TEXT_WEIGHT  = 0.3;

    public boolean passesHardFilter(EbayCandidate item, Set<String> allowed) {
        if (item.categoryIds() == null) return false;
        return item.categoryIds().stream().anyMatch(allowed::contains);
    }

    public double parsePrice(EbayCandidate item) {
        return item.priceUsd();
    }

    public String thumbnailUrl(EbayCandidate item) {
        return item.thumbnailUrl();
    }

    public Optional<SoozipCategory> parseSoozipCategory(String category) {
        return SoozipCategory.fromString(category);
    }

    public double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException(
                    "임베딩 차원이 다릅니다: a=" + a.size() + ", b=" + b.size());
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot   += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
