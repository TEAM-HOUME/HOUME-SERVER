package or.sopt.houme.domain.coupang.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.port.out.EmbeddingPort;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangProductJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoupangProductImageEmbeddingService {

    private final EmbeddingPort embeddingPort;
    private final CoupangProductJpaRepository productRepository;

    public int embedMissingImages(int batchSize) {
        List<CoupangCollectionJobService.CoupangProductImageEmbeddingTarget> targets =
                productRepository.findProductsNeedingImageEmbedding(PageRequest.of(0, batchSize)).stream()
                        .map(product -> new CoupangCollectionJobService.CoupangProductImageEmbeddingTarget(
                                product.getCoupangProductId(), product.getImageUrl()
                        ))
                        .toList();
        embedAndSaveImages(targets);
        return targets.size();
    }

    /**
     * Gemini 호출은 수집 트랜잭션이 종료된 뒤 수행한다. 개별 상품의 실패는 다음 수집에서 재시도한다.
     */
    public void embedAndSaveImages(List<CoupangCollectionJobService.CoupangProductImageEmbeddingTarget> targets) {
        for (CoupangCollectionJobService.CoupangProductImageEmbeddingTarget target : targets) {
            try {
                List<Double> embedding = embeddingPort.embedImageUrl(target.imageUrl());
                productRepository.findByCoupangProductId(target.coupangProductId())
                        .filter(product -> target.imageUrl().equals(product.getImageUrl()))
                        .filter(CoupangProductJpaEntity::needsImageEmbedding)
                        .ifPresent(product -> {
                            product.updateImageEmbedding(embedding);
                            productRepository.save(product);
                        });
            } catch (Exception e) {
                log.warn("쿠팡 상품 이미지 임베딩 실패: productId={}", target.coupangProductId(), e);
            }
        }
    }
}
