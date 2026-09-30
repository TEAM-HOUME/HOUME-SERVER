package or.sopt.houme.domain.furniture.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.AdminCurationEmbeddingUseCase;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCurationEmbeddingService implements AdminCurationEmbeddingUseCase {

    private final CurationEmbeddingBatchService batchService;

    @Override
    public int triggerEmbedding() {
        return batchService.fillMissingEmbeddings();
    }
}
