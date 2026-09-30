package or.sopt.houme.domain.furniture.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.port.out.EmbeddingPort;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurationEmbeddingBatchService {

    private static final int PAGE_SIZE = 50;

    private final CurationRawProductRepository repository;
    private final EmbeddingPort embeddingPort;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public boolean isRunning() {
        return running.get();
    }

    public int fillMissingEmbeddings() {
        return fillMissingEmbeddings(Integer.MAX_VALUE);
    }

    public int fillMissingEmbeddings(int limit) {
        if (!running.compareAndSet(false, true)) {
            log.warn("[임베딩 배치] 이미 실행 중 — 요청 무시");
            return -1;
        }
        try {
            int titleProcessed = fillMissingTitleEmbeddings(limit);
            int imageProcessed = fillMissingImageEmbeddings(limit);
            return titleProcessed + imageProcessed;
        } finally {
            running.set(false);
        }
    }

    private int fillMissingTitleEmbeddings(int limit) {
        int totalAttempted = 0;
        int totalProcessed = 0;
        long lastId = 0;
        while (totalAttempted < limit) {
            int fetchSize = Math.min(PAGE_SIZE, limit - totalAttempted);
            List<CurationRawProduct> batch = repository.findForTitleEmbeddingBatch(lastId, PageRequest.of(0, fetchSize));
            if (batch.isEmpty()) break;

            lastId = batch.get(batch.size() - 1).getId(); // 실패해도 커서 전진 → 이후 상품 차단 방지
            totalAttempted += batch.size();
            totalProcessed += processTitleAndImageBatch(batch);
        }
        return totalProcessed;
    }

    private int fillMissingImageEmbeddings(int limit) {
        if (limit <= 0) return 0;
        int totalAttempted = 0;
        int totalProcessed = 0;
        long lastId = 0;
        while (totalAttempted < limit) {
            int fetchSize = Math.min(PAGE_SIZE, limit - totalAttempted);
            List<CurationRawProduct> batch = repository.findForImageEmbeddingBatch(lastId, PageRequest.of(0, fetchSize));
            if (batch.isEmpty()) break;

            lastId = batch.get(batch.size() - 1).getId();
            totalAttempted += batch.size();
            totalProcessed += processImageOnlyBatch(batch);
        }
        return totalProcessed;
    }

    private int processTitleAndImageBatch(List<CurationRawProduct> products) {
        int count = 0;
        for (CurationRawProduct product : products) {
            try {
                List<Double> titleEmb = embeddingPort.embedText(product.getProductName());
                product.updateTitleEmbedding(toVectorString(titleEmb));

                if (product.getProductImageUrl() != null && !product.getProductImageUrl().isBlank()) {
                    try {
                        List<Double> imageEmb = embeddingPort.embedImageUrl(product.getProductImageUrl());
                        product.updateImageEmbedding(toVectorString(imageEmb));
                    } catch (Exception e) {
                        log.warn("[임베딩 배치] 이미지 임베딩 실패 — title만 저장: productId={}", product.getId(), e);
                    }
                }

                repository.save(product);
                count++;
            } catch (Exception e) {
                log.warn("[임베딩 배치] 상품 스킵: productId={}", product.getId(), e);
            }
        }
        return count;
    }

    private int processImageOnlyBatch(List<CurationRawProduct> products) {
        int count = 0;
        for (CurationRawProduct product : products) {
            try {
                List<Double> imageEmb = embeddingPort.embedImageUrl(product.getProductImageUrl());
                product.updateImageEmbedding(toVectorString(imageEmb));
                repository.save(product);
                count++;
            } catch (Exception e) {
                log.warn("[임베딩 배치] 이미지 재시도 실패: productId={}", product.getId(), e);
            }
        }
        return count;
    }

    private String toVectorString(List<Double> embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
