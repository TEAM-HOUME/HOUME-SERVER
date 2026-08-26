package or.sopt.houme.compare.infrastructure.gemini;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiEmbeddingAdapter {

    private static final String EMBEDDING_MODEL = "gemini-embedding-2";

    private final GeminiEmbeddingClient embeddingClient;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    @Value("${gemini.compare-api-key:}")
    private String apiKey;

    public List<Double> embedText(String text) {
        try {
            GeminiEmbeddingRequest req = GeminiEmbeddingRequest.forText(text);
            GeminiEmbeddingResponse resp = embeddingClient.embedContent(EMBEDDING_MODEL, apiKey, req);
            return resp.embedding().values();
        } catch (FeignException e) {
            log.error("텍스트 임베딩 실패", e);
            throw new CompareException(ErrorCode.COMPARE_EMBEDDING_FAILED);
        }
    }

    public List<Double> embedImageUrl(String imageUrl) {
        try {
            DownloadResult download = downloadWithMimeType(imageUrl);
            GeminiEmbeddingRequest req = GeminiEmbeddingRequest.forImage(download.mimeType(), download.base64());
            GeminiEmbeddingResponse resp = embeddingClient.embedContent(EMBEDDING_MODEL, apiKey, req);
            return resp.embedding().values();
        } catch (FeignException e) {
            log.error("이미지 임베딩 실패: url={}", imageUrl, e);
            throw new CompareException(ErrorCode.COMPARE_EMBEDDING_FAILED);
        }
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
