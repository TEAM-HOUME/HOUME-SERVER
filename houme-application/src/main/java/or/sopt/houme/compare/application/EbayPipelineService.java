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
import or.sopt.houme.compare.domain.CoupangCandidate;
import or.sopt.houme.compare.domain.CurationCandidate;
import or.sopt.houme.compare.domain.MarketplaceSearchKeywords;
import or.sopt.houme.compare.domain.port.out.EbayProductPort;
import or.sopt.houme.compare.domain.port.out.CoupangSearchPort;
import or.sopt.houme.compare.domain.port.out.CurationProductSearchPort;
import or.sopt.houme.compare.domain.port.out.EbaySearchPort;
import or.sopt.houme.compare.domain.port.out.EmbeddingPort;
import or.sopt.houme.compare.domain.port.out.KeywordTranslationPort;
import or.sopt.houme.domain.coupang.service.CoupangPriorityKeywordQueueService;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.furniture.domain.FurnitureWithTypeView;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final ExecutorService EMBED_POOL = Executors.newVirtualThreadPerTaskExecutor();
    private static final double IMAGE_WEIGHT = EbayPipelineUtils.IMAGE_WEIGHT;
    private static final double TEXT_WEIGHT  = EbayPipelineUtils.TEXT_WEIGHT;
    private static final int MAX_RESULTS = 20;

    private final EbaySearchPort ebaySearchPort;
    private final KeywordTranslationPort keywordTranslator;
    private final EmbeddingPort embeddingAdapter;
    private final PriceSoftFilter priceSoftFilter;
    private final EbayProductPort catalogPort;
    private final CoupangSearchPort coupangSearchPort;
    private final CurationProductSearchPort curationSearchPort;
    private final EbayPipelineUtils utils;
    private final or.sopt.houme.compare.domain.port.out.CompareJobStorePort jobStore;
    private final FurnitureRepositoryPort furnitureRepositoryPort;
    private final CoupangPriorityKeywordQueueService coupangPriorityKeywordQueueService;

    @Value("${compare.pipeline.top-n:7}")
    private int topN;

    @Async("imageGenerationExecutor")
    public void runAsync(CompareJob job) {
        try {
            run(job);
        } catch (Exception e) {
            log.error("파이프라인 실행 중 예외 발생: jobId={}", job.getJobId(), e);
            job.markFailed(e.getClass().getSimpleName());
            trySave(job);
        }
    }

    private void trySave(CompareJob job) {
        try {
            jobStore.save(job);
        } catch (Exception e) {
            log.error("Job 상태 저장 실패: jobId={}", job.getJobId(), e);
        }
    }

    private void run(CompareJob job) {
        OriginalProduct original = job.getOriginalProduct();
        long t0 = System.currentTimeMillis();

        job.markRunning(JobStage.SEARCHING);
        trySave(job);

        long t1 = System.currentTimeMillis();
        List<FurnitureWithTypeView> furnitureCandidates = furnitureRepositoryPort.findAllWithType();
        MarketplaceSearchKeywords keywords = keywordTranslator.translateToMarketplaceKeywords(original.title(), furnitureCandidates);
        String keyword = keywords.ebayKeyword();
        String coupangKeyword = keywords.coupangKeyword();
        Long furnitureId = keywords.furnitureId();
        log.info("[타이밍] 키워드 번역: {}ms → ebay='{}', coupang='{}'", System.currentTimeMillis() - t1, keyword, coupangKeyword);

        long t2 = System.currentTimeMillis();
        List<EbayCandidate> items = ebaySearchPort.search(keyword, 200);
        log.info("[타이밍] eBay 검색: {}ms → {}개", System.currentTimeMillis() - t2, items.size());

        Optional<SoozipCategory> soozipCat =
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
        job.markEbayDone();
        trySave(job);

        // 쿠팡 — 저장된 임베딩으로 Java 내 cosine sim 계산 (Gemini 호출 없음)
        try {
            List<CoupangCandidate> coupangCandidates =
                    coupangSearchPort.findCandidatesByKeyword(coupangKeyword);
            if (coupangCandidates.isEmpty()) {
                try {
                    coupangPriorityKeywordQueueService.enqueueIfAbsent(coupangKeyword, furnitureId);
                    log.info("[파이프라인] 쿠팡 캐시 미스 — 수집 큐 등록: keyword={}", coupangKeyword);
                } catch (Exception qe) {
                    log.warn("[파이프라인] 쿠팡 큐 등록 실패: {}", qe.getMessage());
                }
            }
            coupangCandidates.stream()
                    .filter(c -> c.price() != null && priceSoftFilter.passes(originalKrw, c.price()))
                    .forEach(c -> {
                        double textSim = c.titleEmbedding() != null
                                ? utils.cosineSimilarity(origTextEmb, c.titleEmbedding()) : 0.0;
                        double imageSim = (finalOrigImageEmb != null && c.imageEmbedding() != null)
                                ? utils.cosineSimilarity(finalOrigImageEmb, c.imageEmbedding()) : 0.0;
                        double score = IMAGE_WEIGHT * imageSim + TEXT_WEIGHT * textSim;
                        unified.add(new UnifiedCandidate(new SimilarProduct(
                                "COUPANG", c.id() != null ? String.valueOf(c.id()) : null,
                                c.title(), c.imageUrl(), c.price(),
                                "KRW", c.productUrl(), score, List.of()
                        ), score));
                    });
            job.markCoupangDone();
            log.info("[파이프라인] 쿠팡 스코어링 완료");
        } catch (Exception e) {
            job.markCoupangFailed();
            log.warn("[파이프라인] 쿠팡 조회 실패: {}", e.getMessage());
        }
        trySave(job);

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
                        // 찜 API는 source=RAW로만 원천 상품을 조회하므로 source를 RAW로 고정
                        unified.add(new UnifiedCandidate(new SimilarProduct(
                                "RAW", String.valueOf(c.catalogItemId()), c.title(), c.imageUrl(), c.price(),
                                "KRW", c.productUrl(), score, List.of()
                        ), score));
                    });
            job.markCatalogDone();
        } catch (Exception e) {
            job.markCatalogFailed();
            log.warn("[파이프라인] 자체 카탈로그 조회 실패: {}", e.getMessage());
        }
        trySave(job);

        // eBay 후보 카탈로그 저장 — ebayItemId → 내부 ID 맵 획득 후 productId 교체
        Map<String, Long> ebayIdMap = upsertToCatalog(topScored, original.category());

        // 통합 랭킹 — 점수 내림차순 top MAX_RESULTS, EBAY productId를 내부 ID로 치환
        List<SimilarProduct> results = unified.stream()
                .sorted(Comparator.comparingDouble(UnifiedCandidate::score).reversed())
                .limit(MAX_RESULTS)
                .map(c -> {
                    SimilarProduct p = c.product();
                    if ("EBAY".equals(p.source())) {
                        // upsert 실패 시 null — 외부 itemId를 내부 id로 오인하지 않도록
                        Long internalId = p.productId() != null ? ebayIdMap.get(p.productId()) : null;
                        return new SimilarProduct(p.source(), internalId != null ? String.valueOf(internalId) : null,
                                p.title(), p.imageUrl(), p.price(), p.currency(),
                                p.productUrl(), p.similarityScore(), p.categories());
                    }
                    return p;
                })
                .collect(Collectors.toList());

        log.info("[타이밍] 전체 파이프라인: {}ms", System.currentTimeMillis() - t0);
        job.markDone(results);
        trySave(job);
        log.info("파이프라인 완료: jobId={}, results={}", job.getJobId(), results.size());
    }

    private record UnifiedCandidate(SimilarProduct product, double score) {}

    private Map<String, Long> upsertToCatalog(List<ScoredItem> topScored, String soozipCategory) {
        Map<String, Long> idMap = new HashMap<>();
        for (ScoredItem s : topScored) {
            try {
                EbayProduct saved = catalogPort.upsert(EbayProduct.forUpsert(
                        s.item().itemId(), s.item().title(), utils.thumbnailUrl(s.item()),
                        utils.parsePrice(s.item()), s.item().itemWebUrl(), soozipCategory,
                        s.textEmb(), s.imageEmb()
                ));
                if (saved.id() != null) {
                    idMap.put(s.item().itemId(), saved.id());
                }
            } catch (Exception e) {
                log.warn("카탈로그 upsert 실패: itemId={}", s.item().itemId(), e);
            }
        }
        return idMap;
    }

    private SimilarProduct toSimilarProduct(EbayCandidate item, double score) {
        List<SimilarProduct.EbayCategory> cats = item.categoryIds() == null ? List.of() :
                item.categoryIds().stream()
                        .map(id -> new SimilarProduct.EbayCategory(id, null))
                        .collect(Collectors.toList());
        return new SimilarProduct(
                "EBAY", item.itemId(), item.title(), utils.thumbnailUrl(item),
                utils.parsePrice(item) * EbayPipelineUtils.USD_TO_KRW,
                "KRW",
                item.itemWebUrl(), score, cats
        );
    }

    private record ScoredItem(EbayCandidate item, double score, List<Double> textEmb, List<Double> imageEmb) {}
}
