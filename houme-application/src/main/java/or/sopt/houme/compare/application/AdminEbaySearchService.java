package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.application.filter.PriceSoftFilter;
import or.sopt.houme.compare.infrastructure.ebay.EbaySearchAdapter;
import or.sopt.houme.compare.infrastructure.ebay.dto.EbaySearchResponse;
import or.sopt.houme.compare.infrastructure.gemini.GeminiEmbeddingAdapter;
import or.sopt.houme.compare.infrastructure.llm.GeminiKeywordTranslator;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEbaySearchService {

    private static final int ADMIN_TOP_N = 10;

    private final EbaySearchAdapter ebaySearchAdapter;
    private final GeminiKeywordTranslator keywordTranslator;
    private final GeminiEmbeddingAdapter embeddingAdapter;
    private final PriceSoftFilter priceSoftFilter;
    private final EbayPipelineUtils utils;

    public AdminSearchResult textSearch(
            String koreanTitle, String imageUrl, Double priceKrw, String category) {

        String keyword = keywordTranslator.translateToEnglish(koreanTitle);
        log.info("[Admin 텍스트검색] 번역: '{}' → '{}'", koreanTitle, keyword);

        List<EbaySearchResponse.ItemSummary> raw = ebaySearchAdapter.search(keyword, 200);
        FilteredItems filtered = applyFilters(raw, category, priceKrw);
        List<EbaySearchResponse.ItemSummary> candidates = filtered.items().stream().limit(ADMIN_TOP_N).collect(Collectors.toList());

        List<Double> origTextEmb = embeddingAdapter.embedText(koreanTitle);
        List<Double> origImageEmb = embedImageSafe(imageUrl);

        List<AdminSearchCandidate> scored = scoreInParallel(candidates, origTextEmb, origImageEmb);
        FilterStats stats = new FilterStats(filtered.totalFetched(), filtered.afterCategoryFilter(), filtered.afterPriceFilter(), scored.size());
        return new AdminSearchResult(scored, stats);
    }

    public AdminSearchResult imageSearch(
            String imageUrl, Double priceKrw, String category) {

        String base64 = embeddingAdapter.downloadBase64(imageUrl);
        log.info("[Admin 이미지검색] base64 변환 완료: url={}", imageUrl);

        List<EbaySearchResponse.ItemSummary> raw = ebaySearchAdapter.searchByImage(base64, 200);
        FilteredItems filtered = applyFilters(raw, category, priceKrw);
        List<EbaySearchResponse.ItemSummary> candidates = filtered.items().stream().limit(ADMIN_TOP_N).collect(Collectors.toList());

        List<Double> origImageEmb = embedImageSafe(imageUrl);

        List<AdminSearchCandidate> scored = scoreInParallel(candidates, null, origImageEmb);
        FilterStats stats = new FilterStats(filtered.totalFetched(), filtered.afterCategoryFilter(), filtered.afterPriceFilter(), scored.size());
        return new AdminSearchResult(scored, stats);
    }

    private FilteredItems applyFilters(List<EbaySearchResponse.ItemSummary> items, String category, Double priceKrw) {
        int totalFetched = items.size();

        SoozipCategory soozipCat = utils.parseSoozipCategory(category);
        if (soozipCat != null && EbayPipelineUtils.EBAY_CATEGORY_MAP.containsKey(soozipCat)) {
            Set<String> allowed = EbayPipelineUtils.EBAY_CATEGORY_MAP.get(soozipCat);
            items = items.stream().filter(item -> utils.passesHardFilter(item, allowed)).collect(Collectors.toList());
            log.info("[Admin] 하드필터: {}개 → {}개", totalFetched, items.size());
        }
        int afterCategory = items.size();

        if (priceKrw != null) {
            items = items.stream()
                    .filter(item -> priceSoftFilter.passes(priceKrw, utils.parsePrice(item) * EbayPipelineUtils.USD_TO_KRW))
                    .collect(Collectors.toList());
            log.info("[Admin] 소프트필터: {}개", items.size());
        }
        int afterPrice = items.size();

        return new FilteredItems(items, totalFetched, afterCategory, afterPrice);
    }

    private record FilteredItems(List<EbaySearchResponse.ItemSummary> items, int totalFetched, int afterCategoryFilter, int afterPriceFilter) {}

    private List<AdminSearchCandidate> scoreInParallel(
            List<EbaySearchResponse.ItemSummary> candidates,
            List<Double> origTextEmb,
            List<Double> origImageEmb) {

        List<CompletableFuture<AdminSearchCandidate>> futures = candidates.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    double textSim = 0.0;
                    double imageSim = 0.0;

                    if (origTextEmb != null) {
                        List<Double> textEmb = embeddingAdapter.embedText(item.title());
                        textSim = utils.cosineSimilarity(origTextEmb, textEmb);
                    }

                    String thumbUrl = utils.thumbnailUrl(item);
                    if (origImageEmb != null && thumbUrl != null) {
                        try {
                            List<Double> imageEmb = embeddingAdapter.embedImageUrl(thumbUrl);
                            imageSim = utils.cosineSimilarity(origImageEmb, imageEmb);
                        } catch (Exception e) {
                            log.warn("[Admin] 이미지 임베딩 실패: itemId={}", item.itemId(), e);
                        }
                    }

                    double combined = EbayPipelineUtils.IMAGE_WEIGHT * imageSim + EbayPipelineUtils.TEXT_WEIGHT * textSim;
                    return new AdminSearchCandidate(
                            item.title(),
                            utils.thumbnailUrl(item),
                            utils.parsePrice(item),
                            item.itemWebUrl(),
                            textSim,
                            imageSim,
                            combined
                    );
                }))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .sorted((a, b) -> Double.compare(b.combinedScore(), a.combinedScore()))
                .collect(Collectors.toList());
    }

    private List<Double> embedImageSafe(String imageUrl) {
        if (imageUrl == null) return null;
        try {
            return embeddingAdapter.embedImageUrl(imageUrl);
        } catch (Exception e) {
            log.warn("[Admin] 원본 이미지 임베딩 실패: url={}", imageUrl, e);
            return null;
        }
    }

    public record FilterStats(int totalFetched, int afterCategoryFilter, int afterPriceFilter, int scored) {}

    public record AdminSearchResult(List<AdminSearchCandidate> items, FilterStats filterStats) {}

    public record AdminSearchCandidate(
            String title,
            String imageUrl,
            double priceUsd,
            String productUrl,
            double textSimilarity,
            double imageSimilarity,
            double combinedScore
    ) {}
}
