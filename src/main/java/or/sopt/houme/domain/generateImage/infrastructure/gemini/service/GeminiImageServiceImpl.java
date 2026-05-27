package or.sopt.houme.domain.generateImage.infrastructure.gemini.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.client.GeminiImageClient;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.dto.GeminiImageRequest;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.dto.GeminiImageResponse;
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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Profile("!load_test")
@RequiredArgsConstructor
@Slf4j
public class GeminiImageServiceImpl implements GeminiImageService {
    private final GeminiImageClient geminiImageClient;
    private final GeminiImageConfig geminiImageConfig;
    private final S3Util s3Util;
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
        return executeGeminiRequest(promptWithSize, request, defaultModel(geminiImageConfig.getModel()));
    }

    @Override
    public ImageUploadResponseDTO createImageWithReferences(String prompt, List<String> referenceImageUrls) {
        String promptWithSize = applySizeHint(prompt, geminiImageConfig.getSize());
        List<GeminiImageRequest.Part> referenceParts = toReferenceImageParts(referenceImageUrls);
        log.info(
                "event=image.gemini.reference_prepared referenceImageCount={} usableReferenceImageCount={}",
                referenceImageUrls == null ? 0 : referenceImageUrls.size(),
                referenceParts.size()
        );
        GeminiImageRequest request = GeminiImageRequest.of(promptWithSize, referenceParts);
        return executeGeminiRequest(promptWithSize, request, defaultModel(geminiImageConfig.getModel()));
    }

    private ImageUploadResponseDTO executeGeminiRequest(
            String prompt,
            GeminiImageRequest request,
            String model
    ) {
        long startTime = System.nanoTime();
        log.info("event=image.ai.request.started provider=gemini model={} promptLength={}", model, prompt.length());
        try {
            GeminiImageResponse response = geminiImageClient.generateImage(model, apiKey, request);
            byte[] image = decodeBase64(extractBase64(response));
            ImageUploadResponseDTO responseDTO = s3Util.uploadByByte(S3Constant.CHAT_GPT_DIRNAME, image);
            responseDTO.setPullPrompt(prompt);
            log.info(
                    "event=image.ai.request.succeeded provider=gemini model={} durationMs={} imageBytes={}",
                    model,
                    elapsedMillis(startTime),
                    image.length
            );
            return responseDTO;
        } catch (FeignException e) {
            log.error(
                    "event=image.ai.request.failed provider=gemini model={} durationMs={} status={} exceptionType={} message={}",
                    model,
                    elapsedMillis(startTime),
                    e.status(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e
            );
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        } catch (IllegalArgumentException e) {
            log.error(
                    "event=image.ai.response.decode_failed provider=gemini model={} durationMs={} exceptionType={} message={}",
                    model,
                    elapsedMillis(startTime),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e
            );
            throw new S3Exception(ErrorCode.INCODING_EXCEPTION);
        }
    }

    private List<GeminiImageRequest.Part> toReferenceImageParts(List<String> referenceImageUrls) {
        if (referenceImageUrls == null || referenceImageUrls.isEmpty()) {
            return List.of();
        }
        List<GeminiImageRequest.Part> referenceParts = new java.util.ArrayList<>();
        for (String url : referenceImageUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            try {
                DownloadedImageData imageData = downloadImage(url);
                referenceParts.add(GeminiImageRequest.Part.inlineData(imageData.mimeType(), imageData.base64Data()));
            } catch (ChatGptException e) {
                log.warn("event=image.reference.download_failed provider=gemini exceptionType={}", e.getClass().getSimpleName());
            }
        }
        return referenceParts.stream().collect(Collectors.toList());
    }

    private DownloadedImageData downloadImage(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Houme-Gemini/1.0")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
            }

            String mimeType = response.headers()
                    .firstValue("content-type")
                    .map(this::normalizeMimeType)
                    .orElse("image/jpeg");
            String base64 = Base64.getEncoder().encodeToString(response.body());
            return new DownloadedImageData(mimeType, base64);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        } catch (IllegalArgumentException e) {
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

    private String extractBase64(GeminiImageResponse response) {
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
                    return part.inlineData().data();
                }
            }
        }

        throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
    }

    private byte[] decodeBase64(String base64) {
        // base64 문자열을 실제 바이너리 이미지로 변환합니다.
        return Base64.getDecoder().decode(base64);
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

    private long elapsedMillis(long startTime) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    private record DownloadedImageData(String mimeType, String base64Data) {
    }
}
