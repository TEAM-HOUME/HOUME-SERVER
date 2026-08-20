package or.sopt.houme.domain.generateImage.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.model.GeminiImageGenerationObservation;
import or.sopt.houme.domain.generateImage.model.entity.GeminiImageGenerationObservationJpaEntity;
import or.sopt.houme.domain.generateImage.port.out.GeminiImageGenerationObservationPort;
import or.sopt.houme.domain.generateImage.repository.GeminiImageGenerationObservationJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GeminiImageGenerationObservationPersistenceAdapter implements GeminiImageGenerationObservationPort {

    private final GeminiImageGenerationObservationJpaRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(GeminiImageGenerationObservation observation) {
        repository.save(GeminiImageGenerationObservationJpaEntity.builder()
                .apiType(observation.apiType())
                .apiPath(observation.apiPath())
                .userId(observation.userId())
                .traceId(observation.traceId())
                .model(observation.model())
                .promptChars(observation.promptChars())
                .referenceImageCount(observation.referenceImageCount())
                .variantReusedCount(observation.variantReusedCount())
                .runtimeCompressedCount(observation.runtimeCompressedCount())
                .referenceSourceBytes(observation.referenceSourceBytes())
                .referenceOptimizedBytes(observation.referenceOptimizedBytes())
                .referenceBase64Bytes(observation.referenceBase64Bytes())
                .referenceDownloadMillis(observation.referenceDownloadMillis())
                .referenceOptimizationMillis(observation.referenceOptimizationMillis())
                .geminiCallMillis(observation.geminiCallMillis())
                .resultBytes(observation.resultBytes())
                .resultMimeType(observation.resultMimeType())
                .resultDecodeMillis(observation.resultDecodeMillis())
                .s3UploadMillis(observation.s3UploadMillis())
                .totalMillis(observation.totalMillis())
                .finishReason(observation.finishReason())
                .promptTokens(observation.promptTokens())
                .candidateTokens(observation.candidateTokens())
                .totalTokens(observation.totalTokens())
                .thoughtsTokens(observation.thoughtsTokens())
                .cachedContentTokens(observation.cachedContentTokens())
                .resultUrl(observation.resultUrl())
                .build());
    }
}
