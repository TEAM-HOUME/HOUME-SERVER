package or.sopt.houme.compare.application;

import or.sopt.houme.compare.infrastructure.ebay.dto.EbaySearchResponse;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class EbayPipelineUtils {

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

    public boolean passesHardFilter(EbaySearchResponse.ItemSummary item, Set<String> allowed) {
        if (item.categories() == null) return false;
        return item.categories().stream().anyMatch(c -> allowed.contains(c.categoryId()));
    }

    public double parsePrice(EbaySearchResponse.ItemSummary item) {
        if (item.price() == null || item.price().value() == null) return 0.0;
        try {
            return Double.parseDouble(item.price().value());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String thumbnailUrl(EbaySearchResponse.ItemSummary item) {
        if (item.thumbnailImages() == null || item.thumbnailImages().isEmpty()) return null;
        return item.thumbnailImages().get(0).imageUrl();
    }

    public SoozipCategory parseSoozipCategory(String category) {
        if (category == null) return null;
        try {
            return SoozipCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0, normA = 0, normB = 0;
        int size = Math.min(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            dot   += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
