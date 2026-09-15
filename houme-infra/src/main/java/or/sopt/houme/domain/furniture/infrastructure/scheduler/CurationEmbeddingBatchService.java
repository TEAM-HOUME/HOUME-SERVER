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
        int totalProcessed = 0;

        while (true) {
            // 처리 후 titleEmbedding이 채워지면 결과셋에서 빠지므로 항상 page 0 조회
            Page<CurationRawProduct> batch = repository.findAllByTitleEmbeddingIsNull(
                    PageRequest.of(0, PAGE_SIZE)
            );
            if (batch.isEmpty()) {
                break;
            }

            int processed = processBatch(batch.getContent());
            totalProcessed += processed;

            if (processed == 0) {
                // 전부 실패한 경우 무한루프 방지
                break;
            }
        }

        return totalProcessed;
    }

    public int processBatch(List<CurationRawProduct> products) {
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
                        log.warn("[임베딩 배치] 이미지 임베딩 실패 — title 임베딩은 저장: productId={}", product.getId(), e);
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
