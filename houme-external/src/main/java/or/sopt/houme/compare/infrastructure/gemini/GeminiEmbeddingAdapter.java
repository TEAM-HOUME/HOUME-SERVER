package or.sopt.houme.compare.infrastructure.gemini;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.port.out.EmbeddingPort;
import or.sopt.houme.compare.infrastructure.gemini.client.GeminiEmbeddingClient;
import or.sopt.houme.compare.infrastructure.gemini.dto.GeminiEmbeddingRequest;
import or.sopt.houme.compare.infrastructure.gemini.dto.GeminiEmbeddingResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CompareException;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiEmbeddingAdapter implements EmbeddingPort {

    private static final String EMBEDDING_MODEL = "gemini-embedding-2";

    private final GeminiEmbeddingClient embeddingClient;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    @Value("${gemini.compare-api-key:}")
    private String apiKey;

    // ponytail: truncated exponential backoff with jitter — Google-recommended for 429 acceleration limits
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 5_000;
    private static final long MAX_DELAY_MS  = 20_000;

    public List<Double> embedText(String text) {
        GeminiEmbeddingRequest req = GeminiEmbeddingRequest.forText(text);
        return embedWithRetry(() -> embeddingClient.embedContent(EMBEDDING_MODEL, apiKey, req), "text");
    }

    public List<Double> embedImageUrl(String imageUrl) {
        DownloadResult download = downloadWithMimeType(imageUrl);
        GeminiEmbeddingRequest req = GeminiEmbeddingRequest.forImage(download.mimeType(), download.base64());
        return embedWithRetry(() -> embeddingClient.embedContent(EMBEDDING_MODEL, apiKey, req), "image:" + imageUrl);
    }

    private List<Double> embedWithRetry(java.util.concurrent.Callable<GeminiEmbeddingResponse> call, String hint) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return call.call().embedding().values();
            } catch (FeignException e) {
                if (e.status() == 429 && attempt < MAX_RETRIES) {
                    long delay = Math.min(BASE_DELAY_MS * (1L << (attempt - 1)), MAX_DELAY_MS)
                            + ThreadLocalRandom.current().nextLong(1000);
                    log.warn("Gemini 429 (attempt {}/{}) — {}ms 후 재시도: {}", attempt, MAX_RETRIES, delay, hint);
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    log.error("임베딩 실패 (attempt {}): {}", attempt, hint, e);
                    throw new CompareException(ErrorCode.COMPARE_EMBEDDING_FAILED);
                }
            } catch (Exception e) {
                log.error("임베딩 실패: {}", hint, e);
                throw new CompareException(ErrorCode.COMPARE_EMBEDDING_FAILED);
            }
        }
        throw new CompareException(ErrorCode.COMPARE_EMBEDDING_FAILED);
    }

    public String downloadBase64(String url) {
        return downloadWithMimeType(url).base64();
    }

    private DownloadResult downloadWithMimeType(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Houme-Compare/1.0")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("이미지 다운로드 실패: url={}, status={}", url, response.statusCode());
                throw new CompareException(ErrorCode.COMPARE_EMBEDDING_FAILED);
            }
            String mimeType = response.headers().firstValue("Content-Type")
                    .map(ct -> ct.split(";")[0].trim().toLowerCase(Locale.ROOT))
                    .filter(ct -> ct.startsWith("image/"))
                    .orElse("image/jpeg");
            return new DownloadResult(Base64.getEncoder().encodeToString(response.body()), mimeType);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new CompareException(ErrorCode.COMPARE_EMBEDDING_FAILED);
        }
    }

    private record DownloadResult(String base64, String mimeType) {}
}
