package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.application.filter.PriceSoftFilter;
import or.sopt.houme.compare.domain.EbayProduct;
import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.EbayCandidate;
import or.sopt.houme.compare.domain.JobStage;
import or.sopt.houme.compare.domain.OriginalProduct;
import or.sopt.houme.compare.domain.SimilarProduct;
import or.sopt.houme.compare.domain.CurationCandidate;
import or.sopt.houme.compare.domain.port.out.EbayProductPort;
import or.sopt.houme.compare.domain.port.out.CoupangSearchPort;
import or.sopt.houme.compare.domain.port.out.CurationProductSearchPort;
import or.sopt.houme.compare.domain.port.out.EbaySearchPort;
import or.sopt.houme.compare.domain.port.out.EmbeddingPort;
import or.sopt.houme.compare.domain.port.out.KeywordTranslationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EbayPipelineService {

    // ponytail: fixed pool for I/O-bound Gemini calls — EC2 단일 vCPU에서도 병렬 HTTP 대기 가능
    private static final ExecutorService EMBED_POOL = Executors.newFixedThreadPool(10);

    private final EbaySearchPort ebaySearchPort;
    private final KeywordTranslationPort keywordTranslator;
    private final EmbeddingPort embeddingAdapter;
    private final PriceSoftFilter priceSoftFilter;
    private final EbayProductPort catalogPort;
    private final CoupangSearchPort coupangSearchPort;
    private final CurationProductSearchPort curationSearchPort;
    private final EbayPipelineUtils utils;

    @Value("${compare.pipeline.top-n:7}")
    private int topN;

    @Async("imageGenerationExecutor")
    public void runAsync(CompareJob job) {
        try {
            run(job);
        } catch (Exception e) {
            log.error("파이프라인 실행 중 예외 발생: jobId={}", job.getJobId(), e);
            job.markFailed(e.getClass().getSimpleName());
        }
    }

    private void run(CompareJob job) {
        OriginalProduct original = job.getOriginalProduct();
        long t0 = System.currentTimeMillis();

        job.markRunning(JobStage.SEARCHING);

        long t1 = System.currentTimeMillis();
        String keyword = keywordTranslator.translateToEnglish(original.title());
        log.info("[타이밍] 키워드 번역: {}ms → '{}'", System.currentTimeMillis() - t1, keyword);

        long t2 = System.currentTimeMillis();
        List<EbayCandidate> items = ebaySearchPort.search(keyword, 200);
        log.info("[타이밍] eBay 검색: {}ms → {}개", System.currentTimeMillis() - t2, items.size());

        Optional<or.sopt.houme.domain.furniture.model.entity.SoozipCategory> soozipCat =
                utils.parseSoozipCategory(original.category());
        if (soozipCat.isPresent() && EbayPipelineUtils.EBAY_CATEGORY_MAP.containsKey(soozipCat.get())) {
            Set<String> allowed = EbayPipelineUtils.EBAY_CATEGORY_MAP.get(soozipCat.get());
            int before = items.size();
            items = items.stream()
                    .filter(item -> utils.passesHardFilter(item, allowed))
                    .collect(Collectors.toList());
            log.info("[파이프라인] 카테고리 하드필터 후: {}개 → {}개 (category={})", before, items.size(), soozipCat.get());
        } else {
            log.info("[파이프라인] 카테고리 미지정 — 하드필터 스킵");
        }

        Double originalKrw = original.price();
        items = items.stream()
                .filter(item -> {
                    double priceKrw = utils.parsePrice(item) * EbayPipelineUtils.USD_TO_KRW;
                    return priceSoftFilter.passes(originalKrw, priceKrw);
                })
                .collect(Collectors.toList());
        log.info("[파이프라인] 소프트필터 후: {}개", items.size());

        List<EbayCandidate> candidates = items.stream()
                .filter(item -> utils.thumbnailUrl(item) != null)
                .collect(Collectors.toMap(
                        utils::thumbnailUrl,
                        item -> item,
                        (a, b) -> utils.parsePrice(a) <= utils.parsePrice(b) ? a : b,
                        java.util.LinkedHashMap::new
                ))
                .values().stream()
                .limit(topN)
                .collect(Collectors.toList());

        job.advanceStage(JobStage.MERGING);

        long t3 = System.currentTimeMillis();
        List<Double> origTextEmb = embeddingAdapter.embedText(original.title());
        List<Double> origImageEmb = null;
        if (original.imageUrl() != null) {
            try {
                origImageEmb = embeddingAdapter.embedImageUrl(original.imageUrl());
            } catch (Exception e) {
                log.warn("원본 상품 이미지 임베딩 실패: url={}", original.imageUrl(), e);
            }
        }
        log.info("[타이밍] 원본 임베딩: {}ms", System.currentTimeMillis() - t3);

        final List<Double> finalOrigImageEmb = origImageEmb;
        long t4 = System.currentTimeMillis();
        List<CompletableFuture<ScoredItem>> futures = candidates.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    List<Double> textEmb = embeddingAdapter.embedText(item.title());
                    double textSim = utils.cosineSimilarity(origTextEmb, textEmb);
                    double imageSim = 0.0;
                    List<Double> imageEmb = null;
                    String thumbUrl = utils.thumbnailUrl(item);
                    if (finalOrigImageEmb != null && thumbUrl != null) {
                        try {
                            imageEmb = embeddingAdapter.embedImageUrl(thumbUrl);
                            imageSim = utils.cosineSimilarity(finalOrigImageEmb, imageEmb);
                        } catch (Exception e) {
                            log.warn("이미지 임베딩 실패: itemId={}", item.itemId(), e);
                        }
                    }
                    return new ScoredItem(item, IMAGE_WEIGHT * imageSim + TEXT_WEIGHT * textSim, textEmb, imageEmb);
                }, EMBED_POOL))
                .collect(Collectors.toList());

        List<ScoredItem> scored = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        log.info("[타이밍] 후보 {}개 병렬 임베딩+스코어링: {}ms", candidates.size(), System.currentTimeMillis() - t4);

        job.advanceStage(JobStage.SORTING);
        List<ScoredItem> topScored = scored.stream()
                .sorted(Comparator.comparingDouble(ScoredItem::score).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        // eBay 스코어링 결과
        List<UnifiedCandidate> unified = new java.util.ArrayList<>();
        topScored.forEach(s -> unified.add(new UnifiedCandidate(toSimilarProduct(s.item(), s.score()), s.score())));

        // 쿠팡 — 저장된 임베딩으로 Java 내 cosine sim 계산 (Gemini 호출 없음)
        try {
            coupangSearchPort.findCandidatesByKeyword(keyword).stream()
                    .filter(c -> c.price() != null && priceSoftFilter.passes(originalKrw, c.price()))
                    .forEach(c -> {
                        double textSim = c.titleEmbedding() != null
                                ? utils.cosineSimilarity(origTextEmb, c.titleEmbedding()) : 0.0;
                        double imageSim = (finalOrigImageEmb != null && c.imageEmbedding() != null)
                                ? utils.cosineSimilarity(finalOrigImageEmb, c.imageEmbedding()) : 0.0;
                        double score = IMAGE_WEIGHT * imageSim + TEXT_WEIGHT * textSim;
                        unified.add(new UnifiedCandidate(new SimilarProduct(
                                "COUPANG", c.title(), c.imageUrl(), c.price(),
                                "KRW", c.productUrl(), score, List.of()
                        ), score));
                    });
            log.info("[파이프라인] 쿠팡 스코어링 완료");
        } catch (Exception e) {
            log.warn("[파이프라인] 쿠팡 조회 실패: {}", e.getMessage());
        }

        // 자체 카탈로그 — 저장된 임베딩으로 Java 내 cosine sim 계산 (Gemini 호출 없음)
        try {
            String categoryName = soozipCat.map(Enum::name).orElse(null);
            List<CurationCandidate> curationRaw = curationSearchPort.findCandidates(categoryName);
            log.info("[파이프라인] 자체 카탈로그 후보: {}개", curationRaw.size());
            curationRaw.stream()
                    .filter(c -> c.price() != null && priceSoftFilter.passes(originalKrw, c.price()))
                    .forEach(c -> {
                        double textSim = c.titleEmbedding() != null
                                ? utils.cosineSimilarity(origTextEmb, c.titleEmbedding()) : 0.0;
                        double imageSim = (finalOrigImageEmb != null && c.imageEmbedding() != null)
                                ? utils.cosineSimilarity(finalOrigImageEmb, c.imageEmbedding()) : 0.0;
                        double score = IMAGE_WEIGHT * imageSim + TEXT_WEIGHT * textSim;
                        unified.add(new UnifiedCandidate(new SimilarProduct(
                                c.source(), c.title(), c.imageUrl(), c.price(),
                                "KRW", c.productUrl(), score, List.of()
                        ), score));
                    });
        } catch (Exception e) {
            log.warn("[파이프라인] 자체 카탈로그 조회 실패: {}", e.getMessage());
        }

        // 통합 랭킹 — 점수 내림차순 top MAX_RESULTS
        List<SimilarProduct> results = unified.stream()
                .sorted(Comparator.comparingDouble(UnifiedCandidate::score).reversed())
                .limit(MAX_RESULTS)
                .map(UnifiedCandidate::product)
                .collect(Collectors.toList());

        log.info("[타이밍] 전체 파이프라인: {}ms", System.currentTimeMillis() - t0);
        job.markDone(results);
        log.info("파이프라인 완료: jobId={}, results={}", job.getJobId(), results.size());

        upsertToCatalog(topScored, original.category());
    }

    private static final double IMAGE_WEIGHT = EbayPipelineUtils.IMAGE_WEIGHT;
    private static final double TEXT_WEIGHT  = EbayPipelineUtils.TEXT_WEIGHT;
    private static final int MAX_RESULTS = 20;

    private record UnifiedCandidate(SimilarProduct product, double score) {}

    private void upsertToCatalog(List<ScoredItem> topScored, String soozipCategory) {
        for (ScoredItem s : topScored) {
            try {
                catalogPort.upsert(EbayProduct.forUpsert(
                        s.item().itemId(), s.item().title(), utils.thumbnailUrl(s.item()),
                        utils.parsePrice(s.item()), s.item().itemWebUrl(), soozipCategory,
                        s.textEmb(), s.imageEmb()
                ));
            } catch (Exception e) {
                log.warn("카탈로그 upsert 실패: itemId={}", s.item().itemId(), e);
            }
        }
    }

    private SimilarProduct toSimilarProduct(EbayCandidate item, double score) {
        List<SimilarProduct.EbayCategory> cats = item.categoryIds() == null ? List.of() :
                item.categoryIds().stream()
                        .map(id -> new SimilarProduct.EbayCategory(id, null))
                        .collect(Collectors.toList());
        return new SimilarProduct(
                "EBAY", item.title(), utils.thumbnailUrl(item),
                utils.parsePrice(item),
                "USD",
                item.itemWebUrl(), score, cats
        );
    }

    private record ScoredItem(EbayCandidate item, double score, List<Double> textEmb, List<Double> imageEmb) {}
}
