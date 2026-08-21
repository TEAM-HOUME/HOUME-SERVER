package or.sopt.houme.global.image;

import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.model.ReferenceImageCompressionResult;
import or.sopt.houme.domain.generateImage.port.out.ReferenceImageCompressionPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Gemini로 전송할 참고 이미지를 별도 WebP 입력으로 축소한다. */
@Component
@Slf4j
public class GeminiReferenceImageCompressionAdapter implements ReferenceImageCompressionPort {

    private final ImageOptimizer imageOptimizer;
    private final int maxWidth;
    private final int webpQuality;

    public GeminiReferenceImageCompressionAdapter(
            ImageOptimizer imageOptimizer,
            @Value("${image.gemini.max-width:1024}") int maxWidth,
            @Value("${image.gemini.webp-quality:85}") int webpQuality
    ) {
        this.imageOptimizer = imageOptimizer;
        this.maxWidth = maxWidth;
        this.webpQuality = webpQuality;
    }

    @Override
    public ReferenceImageCompressionResult compressForGemini(Path source, long sourceBytes) {
        long startedAt = System.nanoTime();
        ImageOptimizer.ImageSize imageSize = imageOptimizer.readSize(source);

        try {
            // ImageIO가 WebP 등 일부 포맷의 크기를 읽지 못하는 경우에도 cwebp 변환은 시도한다.
            // 원본보다 결과가 커지면 아래 비교에서 원본을 유지하므로 업스케일 결과는 전송하지 않는다.
            int targetWidth = imageSize == null || imageSize.width() <= 0
                    ? maxWidth
                    : Math.min(imageSize.width(), maxWidth);
            byte[] optimized = imageOptimizer.toResizedWebp(source, targetWidth, webpQuality);
            if (optimized.length >= sourceBytes) {
                return original(source, elapsedMillis(startedAt));
            }
            return new ReferenceImageCompressionResult(optimized, true, elapsedMillis(startedAt));
        } catch (ImageOptimizationException e) {
            log.warn("Gemini 참고 이미지 압축 실패. 원본을 사용합니다.", e);
            return original(source, elapsedMillis(startedAt));
        }
    }

    private ReferenceImageCompressionResult original(Path source, long compressionMillis) {
        try {
            return ReferenceImageCompressionResult.original(Files.readAllBytes(source), compressionMillis);
        } catch (IOException e) {
            throw new ImageOptimizationException("Gemini 참고 이미지 원본을 읽을 수 없습니다.", e);
        }
    }

    private long elapsedMillis(long startedAt) {
        return java.time.Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
