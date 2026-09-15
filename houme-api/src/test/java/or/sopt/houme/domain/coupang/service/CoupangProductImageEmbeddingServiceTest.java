package or.sopt.houme.domain.coupang.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.sopt.houme.compare.domain.port.out.EmbeddingPort;
import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangProductJpaRepository;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoupangProductImageEmbeddingServiceTest {

    @Mock
    private EmbeddingPort embeddingPort;
    @Mock
    private CoupangProductJpaRepository productRepository;
    @InjectMocks
    private CoupangProductImageEmbeddingService imageEmbeddingService;

    @Test
    @DisplayName("수집이 끝난 쿠팡 상품의 이미지를 기존 임베딩 포트로 생성해 저장한다")
    void embedsAndSavesImage() {
        String imageUrl = "https://image";
        CoupangProductJpaEntity product = CoupangProductJpaEntity.from(new CoupangProductSearchResult(
                "1", "테스트 소파", new BigDecimal("10000"), BigDecimal.ZERO, imageUrl, "https://product"
        ));
        when(embeddingPort.embedImageUrl(imageUrl)).thenReturn(List.of(0.1, 0.2));
        when(productRepository.findByCoupangProductId("1")).thenReturn(Optional.of(product));

        imageEmbeddingService.embedAndSaveImages(List.of(
                new CoupangCollectionJobService.CoupangProductImageEmbeddingTarget("1", imageUrl)
        ));

        assertThat(product.getImageEmbedding()).isEqualTo("[0.1, 0.2]");
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("기존 수집 결과와 무관하게 임베딩이 비어 있는 상품을 조회해 재시도한다")
    void retriesMissingImageEmbeddings() {
        String imageUrl = "https://image";
        CoupangProductJpaEntity product = CoupangProductJpaEntity.from(new CoupangProductSearchResult(
                "1", "테스트 소파", new BigDecimal("10000"), BigDecimal.ZERO, imageUrl, "https://product"
        ));
        when(productRepository.findProductsNeedingImageEmbedding(any(Pageable.class))).thenReturn(List.of(product));
        when(embeddingPort.embedImageUrl(imageUrl)).thenReturn(List.of(0.1, 0.2));
        when(productRepository.findByCoupangProductId("1")).thenReturn(Optional.of(product));

        int targetCount = imageEmbeddingService.embedMissingImages(10);

        assertThat(targetCount).isEqualTo(1);
        assertThat(product.getImageEmbedding()).isEqualTo("[0.1, 0.2]");
        verify(productRepository).save(product);
    }
}
