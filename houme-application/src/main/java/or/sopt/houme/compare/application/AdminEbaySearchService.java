package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.application.filter.PriceSoftFilter;
import or.sopt.houme.compare.domain.EbayCandidate;
import or.sopt.houme.compare.domain.port.out.EbaySearchPort;
import or.sopt.houme.compare.domain.port.out.EmbeddingPort;
import or.sopt.houme.compare.domain.port.out.KeywordTranslationPort;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 어드민 의사결정용 eBay 검색 서비스.
 * 텍스트/이미지 두 가지 검색 방식의 유사도를 비교해 파이프라인 로직(가중치, 필터 기준)을 결정한다.
 *
 * 흐름: eBay 검색(200개) → 카테고리 하드필터 → 가격 소프트필터 → 상위 N개 임베딩 스코어링
 * combinedScore = imageSim × 0.7 + textSim × 0.3 (가중치는 실험 후 조정 예정)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEbaySearchService {

    private static final int ADMIN_TOP_N = 10;

    private final EbaySearchPort ebaySearchPort;
    private final KeywordTranslationPort keywordTranslator;
    private final EmbeddingPort embeddingAdapter;
    private final PriceSoftFilter priceSoftFilter;
    private final EbayPipelineUtils utils;

    public AdminSearchResult textSearch(
            String koreanTitle, String imageUrl, Double priceKrw, String category) {

        // 1. 한글 상품명 → eBay 영어 검색 키워드 변환
        String keyword = keywordTranslator.translateToEnglish(koreanTitle);
        log.info("[Admin 텍스트검색] 번역: '{}' → '{}'", koreanTitle, keyword);

        // 2. eBay 키워드 검색 → 필터 → 상위 N개
        List<EbayCandidate> raw = ebaySearchPort.search(keyword, 200);
        FilteredItems filtered = applyFilters(raw, category, priceKrw);
        List<EbayCandidate> candidates = filtered.items().stream().limit(ADMIN_TOP_N).collect(Collectors.toList());

        // 3. 원본 상품 임베딩 (텍스트 + 이미지)
        List<Double> origTextEmb = embeddingAdapter.embedText(koreanTitle);
        List<Double> origImageEmb = embedImageSafe(imageUrl);

        // 4. 후보 상품들 병렬 임베딩 → 유사도 스코어링
        List<AdminSearchCandidate> scored = scoreInParallel(candidates, origTextEmb, origImageEmb);
        FilterStats stats = new FilterStats(filtered.totalFetched(), filtered.afterCategoryFilter(), filtered.afterPriceFilter(), scored.size());
        return new AdminSearchResult(scored, stats);
    }

    public AdminSearchResult imageSearch(
            String imageUrl, Double priceKrw, String category) {

        // 1. 이미지 URL → base64 → eBay 이미지 검색
        String base64 = embeddingAdapter.downloadBase64(imageUrl);
        log.info("[Admin 이미지검색] base64 변환 완료: url={}", imageUrl);

        // 2. eBay 이미지 검색 → 필터 → 상위 N개
        List<EbayCandidate> raw = ebaySearchPort.searchByImage(base64, 200);
        FilteredItems filtered = applyFilters(raw, category, priceKrw);
        List<EbayCandidate> candidates = filtered.items().stream().limit(ADMIN_TOP_N).collect(Collectors.toList());

        // 3. 원본 이미지 임베딩만 사용 (텍스트 없음)
        List<Double> origImageEmb = embedImageSafe(imageUrl);

        List<AdminSearchCandidate> scored = scoreInParallel(candidates, null, origImageEmb);
        FilterStats stats = new FilterStats(filtered.totalFetched(), filtered.afterCategoryFilter(), filtered.afterPriceFilter(), scored.size());
        return new AdminSearchResult(scored, stats);
    }

    /**
     * 카테고리 하드필터(eBay categoryId 일치) → 가격 소프트필터(로그 스케일 허용 범위) 순으로 적용.
     * 각 단계별 잔존 개수를 FilteredItems에 기록해 어드민 뷰에 표시한다.
     */
    private FilteredItems applyFilters(List<EbayCandidate> items, String category, Double priceKrw) {
        int totalFetched = items.size();

        Optional<SoozipCategory> soozipCat = utils.parseSoozipCategory(category);
        if (soozipCat.isPresent() && EbayPipelineUtils.EBAY_CATEGORY_MAP.containsKey(soozipCat.get())) {
            Set<String> allowed = EbayPipelineUtils.EBAY_CATEGORY_MAP.get(soozipCat.get());
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

    private record FilteredItems(List<EbayCandidate> items, int totalFetched, int afterCategoryFilter, int afterPriceFilter) {}

    /**
     * 후보 상품들을 병렬로 임베딩 → combinedScore 계산 → 내림차순 정렬.
     * origTextEmb가 null이면 텍스트 유사도는 0으로 처리(이미지 검색 경로).
     */
    private List<AdminSearchCandidate> scoreInParallel(
            List<EbayCandidate> candidates,
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
