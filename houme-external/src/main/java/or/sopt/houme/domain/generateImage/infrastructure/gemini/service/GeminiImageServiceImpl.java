package or.sopt.houme.domain.generateImage.infrastructure.gemini.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.client.GeminiImageClient;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.dto.GeminiImageRequest;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.dto.GeminiImageResponse;
import or.sopt.houme.domain.generateImage.model.GeminiImageGenerationObservation;
import or.sopt.houme.domain.generateImage.model.ReferenceImageCompressionResult;
import or.sopt.houme.domain.generateImage.port.out.GeminiImageGenerationObservationPort;
import or.sopt.houme.domain.generateImage.port.out.ReferenceImageCompressionPort;
import or.sopt.houme.domain.generateImage.port.out.ReferenceImageVariantPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.ChatGptException;
import or.sopt.houme.global.api.handler.S3Exception;
import or.sopt.houme.global.config.GeminiImageConfig;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.S3Util;
import or.sopt.houme.global.util.constant.S3Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Service
@Profile("!load_test")
@RequiredArgsConstructor
@Slf4j
public class GeminiImageServiceImpl implements GeminiImageService {
    private final GeminiImageClient geminiImageClient;
    private final GeminiImageConfig geminiImageConfig;
    private final S3Util s3Util;
    private final GeminiImageGenerationObservationPort observationPort;
    private final ReferenceImageCompressionPort referenceImageCompressionPort;
    private final ReferenceImageVariantPort referenceImageVariantPort;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Override
    public ImageUploadResponseDTO createImage(String prompt) {
        // Gemini는 해상도 파라미터를 받지 않으므로 프롬프트 힌트로만 전달합니다.
        String promptWithSize = applySizeHint(prompt, geminiImageConfig.getSize());
        GeminiImageRequest request = GeminiImageRequest.of(promptWithSize);
        return executeGeminiRequest(
                promptWithSize,
                request,
                defaultModel(geminiImageConfig.getModel()),
                ReferenceImageObservation.empty()
        );
    }

    @Override
    public ImageUploadResponseDTO createImageWithReferences(String prompt, List<String> referenceImageUrls) {
        String promptWithSize = applySizeHint(prompt, geminiImageConfig.getSize());
        ReferenceImageObservation referenceObservation = toReferenceImageParts(referenceImageUrls);
        GeminiImageRequest request = GeminiImageRequest.of(promptWithSize, referenceObservation.parts());
        return executeGeminiRequest(
                promptWithSize,
                request,
                defaultModel(geminiImageConfig.getModel()),
                referenceObservation
        );
    }

    private ImageUploadResponseDTO executeGeminiRequest(
            String prompt,
            GeminiImageRequest request,
            String model,
            ReferenceImageObservation referenceObservation
    ) {
        long totalStartedAt = System.nanoTime();
        try {
            long geminiStartedAt = System.nanoTime();
            GeminiImageResponse response = geminiImageClient.generateImage(model, apiKey, request);
            long geminiCallMillis = elapsedMillis(geminiStartedAt);

            GeneratedImageData generatedImage = extractGeneratedImage(response);
            long decodeStartedAt = System.nanoTime();
            byte[] image = decodeBase64(generatedImage.base64Data());
            long resultDecodeMillis = elapsedMillis(decodeStartedAt);

            long uploadStartedAt = System.nanoTime();
            ImageUploadResponseDTO responseDTO = s3Util.uploadByByte(S3Constant.CHAT_GPT_DIRNAME, image);
            long s3UploadMillis = elapsedMillis(uploadStartedAt);
            responseDTO.setPullPrompt(prompt);
            Long promptTokens = usageValue(response, GeminiImageResponse.UsageMetadata::promptTokenCount);
            Long candidateTokens = usageValue(response, GeminiImageResponse.UsageMetadata::candidatesTokenCount);
            Long totalTokens = usageValue(response, GeminiImageResponse.UsageMetadata::totalTokenCount);
            Long thoughtsTokens = usageValue(response, GeminiImageResponse.UsageMetadata::thoughtsTokenCount);
            Long cachedContentTokens = usageValue(response, GeminiImageResponse.UsageMetadata::cachedContentTokenCount);
            long totalMillis = elapsedMillis(totalStartedAt);
            log.info(
                    "Gemini 이미지 생성 관측 model={}, promptChars={}, referenceImageCount={}, referenceSourceBytes={}, "
                            + "referenceOptimizedBytes={}, referenceBase64Bytes={}, referenceDownloadMillis={}, "
                            + "referenceOptimizationMillis={}, geminiCallMillis={}, "
                            + "resultBytes={}, resultMimeType={}, resultDecodeMillis={}, s3UploadMillis={}, totalMillis={}, "
                            + "finishReason={}, promptTokens={}, candidateTokens={}, totalTokens={}, thoughtsTokens={}, "
                            + "cachedContentTokens={}, resultUrl={}",
                    model,
                    prompt.length(),
                    referenceObservation.parts().size(),
                    referenceObservation.totalSourceBytes(),
                    referenceObservation.totalOptimizedBytes(),
                    referenceObservation.totalBase64Bytes(),
                    referenceObservation.totalDownloadMillis(),
                    referenceObservation.totalOptimizationMillis(),
                    geminiCallMillis,
                    image.length,
                    generatedImage.mimeType(),
                    resultDecodeMillis,
                    s3UploadMillis,
                    totalMillis,
                    generatedImage.finishReason(),
                    promptTokens,
                    candidateTokens,
                    totalTokens,
                    thoughtsTokens,
                    cachedContentTokens,
                    responseDTO.getImageLink()
            );
            saveObservation(
                    model,
                    prompt,
                    referenceObservation,
                    geminiCallMillis,
                    image.length,
                    generatedImage,
                    resultDecodeMillis,
                    s3UploadMillis,
                    totalMillis,
                    promptTokens,
                    candidateTokens,
                    totalTokens,
                    thoughtsTokens,
                    cachedContentTokens,
                    responseDTO.getImageLink()
            );
            return responseDTO;
        } catch (FeignException e) {
            log.info(e.getMessage());
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        } catch (IllegalArgumentException e) {
            throw new S3Exception(ErrorCode.INCODING_EXCEPTION);
        }
    }

    private ReferenceImageObservation toReferenceImageParts(List<String> referenceImageUrls) {
        if (referenceImageUrls == null || referenceImageUrls.isEmpty()) {
            return ReferenceImageObservation.empty();
        }
        List<GeminiImageRequest.Part> referenceParts = new ArrayList<>();
        long totalSourceBytes = 0;
        long totalOptimizedBytes = 0;
        long totalBase64Bytes = 0;
        long totalDownloadMillis = 0;
        long totalOptimizationMillis = 0;
        for (String url : referenceImageUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            try {
                String variantUrl = referenceImageVariantPort.findVariantUrl(url).orElse(url);
                boolean variantReused = !variantUrl.equals(url);
                long downloadStartedAt = System.nanoTime();
                DownloadedImageData imageData = downloadImage(variantUrl);
                long downloadMillis = elapsedMillis(downloadStartedAt);
                try {
                    ReferenceImageCompressionResult compressionResult = variantReused
                            ? readPreOptimizedReferenceImage(imageData.sourcePath())
                            : compressReferenceImage(imageData.sourcePath(), imageData.sourceBytes());
                    String base64 = Base64.getEncoder().encodeToString(compressionResult.bytes());
                    String mimeType = variantReused || compressionResult.compressed() ? "image/webp" : imageData.mimeType();
                    referenceParts.add(GeminiImageRequest.Part.inlineData(mimeType, base64));
                    totalSourceBytes += imageData.sourceBytes();
                    totalOptimizedBytes += compressionResult.bytes().length;
                    totalBase64Bytes += base64.length();
                    totalDownloadMillis += downloadMillis;
                    totalOptimizationMillis += compressionResult.compressionMillis();
                    log.info(
                            "Gemini 참고 이미지 관측 sourceMimeType={}, requestMimeType={}, sourceBytes={}, optimizedBytes={}, "
                            + "base64Bytes={}, downloadMillis={}, optimizationMillis={}, compressed={}, variantReused={}",
                            imageData.mimeType(),
                            mimeType,
                            imageData.sourceBytes(),
                            compressionResult.bytes().length,
                            base64.length(),
                            downloadMillis,
                            compressionResult.compressionMillis(),
                            compressionResult.compressed(),
                            variantReused
                    );
                } finally {
                    deleteQuietly(imageData.sourcePath());
                }
            } catch (ChatGptException e) {
                log.warn("참조 이미지 다운로드 실패. 해당 URL은 건너뜁니다. url={}", url);
            }
        }
        return new ReferenceImageObservation(
                List.copyOf(referenceParts),
                totalSourceBytes,
                totalOptimizedBytes,
                totalBase64Bytes,
                totalDownloadMillis,
                totalOptimizationMillis
        );
    }

    private DownloadedImageData downloadImage(String url) {
        Path downloadPath = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Houme-Gemini/1.0")
                    .GET()
                    .build();

            downloadPath = Files.createTempFile("houme-gemini-reference-", ".img");
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(downloadPath));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                deleteQuietly(downloadPath);
                throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
            }

            String mimeType = response.headers()
                    .firstValue("content-type")
                    .map(this::normalizeMimeType)
                    .orElse("image/jpeg");
            return new DownloadedImageData(mimeType, response.body(), Files.size(response.body()));
        } catch (IOException | InterruptedException e) {
            deleteQuietly(downloadPath);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        } catch (IllegalArgumentException e) {
            deleteQuietly(downloadPath);
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }
    }

    private String normalizeMimeType(String contentType) {
        String lowered = contentType.toLowerCase(Locale.ROOT);
        int separator = lowered.indexOf(';');
        String mimeType = separator >= 0 ? lowered.substring(0, separator).trim() : lowered.trim();
        if (!mimeType.startsWith("image/")) {
            return "image/jpeg";
        }
        return mimeType;
    }

    private GeneratedImageData extractGeneratedImage(GeminiImageResponse response) {
        // 후보 목록에서 첫 번째 이미지(base64)를 찾아 반환합니다.
        if (response == null || response.candidates() == null) {
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }

        for (GeminiImageResponse.Candidate candidate : response.candidates()) {
            if (candidate == null || candidate.content() == null || candidate.content().parts() == null) {
                continue;
            }
            for (GeminiImageResponse.Part part : candidate.content().parts()) {
                if (part != null && part.inlineData() != null && part.inlineData().data() != null) {
                    return new GeneratedImageData(
                            part.inlineData().data(),
                            part.inlineData().mimeType(),
                            candidate.finishReason()
                    );
                }
            }
        }

        throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
    }

    private byte[] decodeBase64(String base64) {
        // base64 문자열을 실제 바이너리 이미지로 변환합니다.
        return Base64.getDecoder().decode(base64);
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }

    private Long usageValue(
            GeminiImageResponse response,
            Function<GeminiImageResponse.UsageMetadata, Integer> extractor
    ) {
        if (response == null || response.usageMetadata() == null) {
            return null;
        }
        Integer value = extractor.apply(response.usageMetadata());
        return value == null ? null : value.longValue();
    }

    private ReferenceImageCompressionResult compressReferenceImage(Path sourcePath, long sourceBytes) {
        try {
            return referenceImageCompressionPort.compressForGemini(sourcePath, sourceBytes);
        } catch (RuntimeException e) {
            log.warn("Gemini 참고 이미지 압축 중 예외가 발생했습니다. 원본을 사용합니다.", e);
            try {
                return ReferenceImageCompressionResult.original(Files.readAllBytes(sourcePath), 0);
            } catch (IOException ioException) {
                throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
            }
        }
    }

    private ReferenceImageCompressionResult readPreOptimizedReferenceImage(Path sourcePath) {
        try {
            return ReferenceImageCompressionResult.original(Files.readAllBytes(sourcePath), 0);
        } catch (IOException e) {
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Gemini 참고 이미지 임시 파일 삭제에 실패했습니다. path={}", path, e);
        }
    }

    private void saveObservation(
            String model,
            String prompt,
            ReferenceImageObservation referenceObservation,
            long geminiCallMillis,
            long resultBytes,
            GeneratedImageData generatedImage,
            long resultDecodeMillis,
            long s3UploadMillis,
            long totalMillis,
            Long promptTokens,
            Long candidateTokens,
            Long totalTokens,
            Long thoughtsTokens,
            Long cachedContentTokens,
            String resultUrl
    ) {
        ImageGenerationRequestContext requestContext = resolveRequestContext();
        try {
            observationPort.save(new GeminiImageGenerationObservation(
                    requestContext.apiType(),
                    requestContext.apiPath(),
                    requestContext.userId(),
                    requestContext.traceId(),
                    model,
                    prompt.length(),
                    referenceObservation.parts().size(),
                    referenceObservation.totalSourceBytes(),
                    referenceObservation.totalOptimizedBytes(),
                    referenceObservation.totalBase64Bytes(),
                    referenceObservation.totalDownloadMillis(),
                    referenceObservation.totalOptimizationMillis(),
                    geminiCallMillis,
                    resultBytes,
                    generatedImage.mimeType(),
                    resultDecodeMillis,
                    s3UploadMillis,
                    totalMillis,
                    generatedImage.finishReason(),
                    promptTokens,
                    candidateTokens,
                    totalTokens,
                    thoughtsTokens,
                    cachedContentTokens,
                    resultUrl
            ));
        } catch (RuntimeException e) {
            log.warn("Gemini 이미지 생성 관측 DB 저장에 실패했습니다. 이미지 생성 결과는 유지합니다.", e);
        }
    }

    private ImageGenerationRequestContext resolveRequestContext() {
        String apiPath = "UNKNOWN";
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            apiPath = attributes.getRequest().getRequestURI();
        }
        return new ImageGenerationRequestContext(
                resolveApiType(apiPath),
                apiPath,
                parseLongOrNull(MDC.get("userId")),
                MDC.get("traceId")
        );
    }

    private String resolveApiType(String apiPath) {
        return switch (apiPath) {
            case "/api/v4/generated-images/generate" -> "V4";
            case "/api/v1/generated-images/generate/banner" -> "BANNER";
            case "/api/v1/generated-images/generate/other-style" -> "STYLE";
            case "/api/v1/generated-images/generate/products" -> "PRODUCT";
            default -> "UNKNOWN";
        };
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String defaultModel(String model) {
        // 설정이 비어있으면 기본 Gemini 이미지 모델을 사용합니다.
        if (model == null || model.isBlank()) {
            return "gemini-3-pro-image-preview";
        }
        return model;
    }

    private String applySizeHint(String prompt, String size) {
        // Gemini가 해상도 파라미터를 받지 않으므로 프롬프트에 힌트만 추가합니다.
        String normalized = normalizeSize(size);
        if (normalized == null) {
            return prompt;
        }
        return prompt + "\n\nResolution: " + normalized + ".";
    }

    private String normalizeSize(String size) {
        // 1k/2k/4k 또는 1024x1024 같은 입력을 표준 해상도로 정규화합니다.
        if (size == null || size.isBlank()) {
            return null;
        }
        String trimmed = size.trim().toLowerCase();
        if ("1k".equals(trimmed)) return "1024x1024";
        if ("2k".equals(trimmed)) return "2048x2048";
        if ("4k".equals(trimmed)) return "4096x4096";
        if (trimmed.matches("\\d{3,4}x\\d{3,4}")) {
            return trimmed;
        }
        return null;
    }

    private record DownloadedImageData(String mimeType, Path sourcePath, long sourceBytes) {
    }

    private record GeneratedImageData(String base64Data, String mimeType, String finishReason) {
    }

    private record ReferenceImageObservation(
            List<GeminiImageRequest.Part> parts,
            long totalSourceBytes,
            long totalOptimizedBytes,
            long totalBase64Bytes,
            long totalDownloadMillis,
            long totalOptimizationMillis
    ) {
        private static ReferenceImageObservation empty() {
            return new ReferenceImageObservation(List.of(), 0, 0, 0, 0, 0);
        }
    }

    private record ImageGenerationRequestContext(String apiType, String apiPath, Long userId, String traceId) {
    }
}
