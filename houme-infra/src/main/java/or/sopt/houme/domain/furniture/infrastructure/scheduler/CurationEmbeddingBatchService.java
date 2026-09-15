package or.sopt.houme.domain.furniture.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.port.out.EmbeddingPort;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurationEmbeddingBatchService {

    private static final int PAGE_SIZE = 50;

    private final CurationRawProductRepository repository;
    private final EmbeddingPort embeddingPort;

    public int fillMissingEmbeddings() {
        int titleProcessed = fillMissingTitleEmbeddings();
        int imageProcessed = fillMissingImageEmbeddings();
        return titleProcessed + imageProcessed;
    }

    private int fillMissingTitleEmbeddings() {
        int totalProcessed = 0;
        while (true) {
            Page<CurationRawProduct> batch = repository.findAllByTitleEmbeddingIsNull(PageRequest.of(0, PAGE_SIZE));
            if (batch.isEmpty()) break;

            int processed = processTitleAndImageBatch(batch.getContent());
            totalProcessed += processed;
            if (processed == 0) break; // 전부 실패 — 무한루프 방지
        }
        return totalProcessed;
    }

    private int fillMissingImageEmbeddings() {
        int totalProcessed = 0;
        while (true) {
            // title은 있지만 image가 없는 상품 재시도 (이전 배치에서 이미지 실패한 케이스)
            Page<CurationRawProduct> batch = repository.findAllByImageEmbeddingMissing(PageRequest.of(0, PAGE_SIZE));
            if (batch.isEmpty()) break;

            int processed = processImageOnlyBatch(batch.getContent());
            totalProcessed += processed;
            if (processed == 0) break;
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
