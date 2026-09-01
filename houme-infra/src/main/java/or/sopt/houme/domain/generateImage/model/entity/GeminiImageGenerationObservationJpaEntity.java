package or.sopt.houme.domain.generateImage.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "gemini_image_generation_observations")
public class GeminiImageGenerationObservationJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_type", nullable = false, length = 30)
    private String apiType;

    @Column(name = "api_path", nullable = false, length = 255)
    private String apiPath;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "prompt_chars", nullable = false)
    private int promptChars;

    @Column(name = "reference_image_count", nullable = false)
    private int referenceImageCount;

    // 기존 관측 행은 요청·스킵 수를 알 수 없으므로 nullable로 유지한다.
    @Column(name = "requested_reference_image_count")
    private Integer requestedReferenceImageCount;

    @Column(name = "skipped_reference_image_count")
    private Integer skippedReferenceImageCount;

    // 기존 관측 행은 variant 재사용 여부를 알 수 없으므로 nullable로 유지한다.
    @Column(name = "variant_reused_count")
    private Integer variantReusedCount;

    @Column(name = "runtime_compressed_count")
    private Integer runtimeCompressedCount;

    // 초기 관측 기능 도입 전 행에는 성능 측정값이 없을 수 있으므로 nullable로 유지한다.
    @Column(name = "reference_source_bytes")
    private Long referenceSourceBytes;

    @Column(name = "reference_optimized_bytes")
    private Long referenceOptimizedBytes;

    @Column(name = "reference_base64_bytes")
    private Long referenceBase64Bytes;

    @Column(name = "reference_download_millis")
    private Long referenceDownloadMillis;

    @Column(name = "reference_optimization_millis")
    private Long referenceOptimizationMillis;

    @Column(name = "gemini_call_millis")
    private Long geminiCallMillis;

    @Column(name = "result_bytes")
    private Long resultBytes;

    @Column(name = "result_mime_type", length = 100)
    private String resultMimeType;

    @Column(name = "result_decode_millis")
    private Long resultDecodeMillis;

    @Column(name = "s3_upload_millis")
    private Long s3UploadMillis;

    @Column(name = "total_millis")
    private Long totalMillis;

    @Column(name = "finish_reason", length = 100)
    private String finishReason;

    @Column(name = "prompt_tokens")
    private Long promptTokens;

    @Column(name = "candidate_tokens")
    private Long candidateTokens;

    @Column(name = "total_tokens")
    private Long totalTokens;

    @Column(name = "thoughts_tokens")
    private Long thoughtsTokens;

    @Column(name = "cached_content_tokens")
    private Long cachedContentTokens;

    @Column(name = "result_url", columnDefinition = "TEXT")
    private String resultUrl;

    @Builder
    private GeminiImageGenerationObservationJpaEntity(
            String apiType,
            String apiPath,
            Long userId,
            String traceId,
            String model,
            int promptChars,
            int referenceImageCount,
            Integer requestedReferenceImageCount,
            Integer skippedReferenceImageCount,
            Integer variantReusedCount,
            Integer runtimeCompressedCount,
            Long referenceSourceBytes,
            Long referenceOptimizedBytes,
            Long referenceBase64Bytes,
            Long referenceDownloadMillis,
            Long referenceOptimizationMillis,
            Long geminiCallMillis,
            Long resultBytes,
            String resultMimeType,
            Long resultDecodeMillis,
            Long s3UploadMillis,
            Long totalMillis,
            String finishReason,
            Long promptTokens,
            Long candidateTokens,
            Long totalTokens,
            Long thoughtsTokens,
            Long cachedContentTokens,
            String resultUrl
    ) {
        this.apiType = apiType;
        this.apiPath = apiPath;
        this.userId = userId;
        this.traceId = traceId;
        this.model = model;
        this.promptChars = promptChars;
        this.referenceImageCount = referenceImageCount;
        this.requestedReferenceImageCount = requestedReferenceImageCount;
        this.skippedReferenceImageCount = skippedReferenceImageCount;
        this.variantReusedCount = variantReusedCount;
        this.runtimeCompressedCount = runtimeCompressedCount;
        this.referenceSourceBytes = referenceSourceBytes;
        this.referenceOptimizedBytes = referenceOptimizedBytes;
        this.referenceBase64Bytes = referenceBase64Bytes;
        this.referenceDownloadMillis = referenceDownloadMillis;
        this.referenceOptimizationMillis = referenceOptimizationMillis;
        this.geminiCallMillis = geminiCallMillis;
        this.resultBytes = resultBytes;
        this.resultMimeType = resultMimeType;
        this.resultDecodeMillis = resultDecodeMillis;
        this.s3UploadMillis = s3UploadMillis;
        this.totalMillis = totalMillis;
        this.finishReason = finishReason;
        this.promptTokens = promptTokens;
        this.candidateTokens = candidateTokens;
        this.totalTokens = totalTokens;
        this.thoughtsTokens = thoughtsTokens;
        this.cachedContentTokens = cachedContentTokens;
        this.resultUrl = resultUrl;
    }
}
