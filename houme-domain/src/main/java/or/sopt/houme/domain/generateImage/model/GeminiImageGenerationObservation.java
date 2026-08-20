package or.sopt.houme.domain.generateImage.model;

/**
 * Gemini 이미지 생성 1건의 입력량·처리 시간·응답 메타데이터를 표현한다.
 * 관측 실패가 이미지 생성 성공 여부에 영향을 주지 않도록 영속은 별도 포트로 위임한다.
 */
public record GeminiImageGenerationObservation(
        String apiType,
        String apiPath,
        Long userId,
        String traceId,
        String model,
        int promptChars,
        int referenceImageCount,
        Integer variantReusedCount,
        Integer runtimeCompressedCount,
        long referenceSourceBytes,
        long referenceOptimizedBytes,
        long referenceBase64Bytes,
        long referenceDownloadMillis,
        long referenceOptimizationMillis,
        long geminiCallMillis,
        long resultBytes,
        String resultMimeType,
        long resultDecodeMillis,
        long s3UploadMillis,
        long totalMillis,
        String finishReason,
        Long promptTokens,
        Long candidateTokens,
        Long totalTokens,
        Long thoughtsTokens,
        Long cachedContentTokens,
        String resultUrl
) {
}
