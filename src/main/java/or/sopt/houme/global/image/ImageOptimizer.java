package or.sopt.houme.global.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * 원본 이미지(byte[])를 cwebp로 리사이즈 + WebP 변환하는 유틸
 *
 * cwebp는 도커 이미지에 포함된 정적 바이너리({@code /app/bin/cwebp})를 실행하므로,
 * 디코딩/리사이즈/인코딩이 JVM heap이 아닌 별도 프로세스(native 메모리)에서 수행됨
 * => 서버의 고정 heap 제약 영향 X, 대용량 이미지로 인한 JVM OOM 위험 X
 *
 * 단, native 메모리는 컨테이너 메모리를 사용하므로 동시 실행 개수는 호출 측에서 제한 필요
 */
@Component
@Slf4j
public class ImageOptimizer {

    private static final long PROCESS_TIMEOUT_SECONDS = 30L;

    private final String cwebpPath;

    public ImageOptimizer(@Value("${image.cwebp.path:/app/bin/cwebp}") String cwebpPath) {
        this.cwebpPath = cwebpPath;
    }

    /**
     * 원본 이미지를 지정한 가로 너비로 리사이즈하고 WebP로 변환
     * 세로 높이는 원본 비율에 맞춰 자동 계산(cwebp {@code -resize width 0}).
     *
     * @param source 원본 이미지 바이트 (jpg/png 등)
     * @param width  변환 결과의 가로 픽셀 너비
     * @return WebP로 변환된 이미지 바이트
     * @throws ImageOptimizationException 변환에 실패한 경우 (원본은 호출 측에서 보존)
     */
    public byte[] toResizedWebp(byte[] source, int width) {
        Path input = null;
        Path output = null;
        try {
            input = Files.createTempFile("houme-img-src-", ".bin");
            output = Files.createTempFile("houme-img-out-", ".webp");
            Files.write(input, source);

            Process process = new ProcessBuilder(
                    cwebpPath, "-quiet",
                    "-resize", String.valueOf(width), "0",
                    input.toString(), "-o", output.toString())
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new ImageOptimizationException(
                        "cwebp 변환이 " + PROCESS_TIMEOUT_SECONDS + "초 내에 완료되지 않았습니다. width=" + width);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String processLog = new String(process.getInputStream().readAllBytes()).trim();
                throw new ImageOptimizationException(
                        "cwebp 변환 실패. exitCode=" + exitCode + ", width=" + width + ", log=" + processLog);
            }

            byte[] result = Files.readAllBytes(output);
            if (result.length == 0) {
                throw new ImageOptimizationException("cwebp 변환 결과가 비어 있습니다. width=" + width);
            }
            return result;

        } catch (IOException e) {
            throw new ImageOptimizationException("이미지 변환 중 IO 오류가 발생했습니다. width=" + width, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImageOptimizationException("이미지 변환이 중단되었습니다. width=" + width, e);
        } finally {
            deleteQuietly(input);
            deleteQuietly(output);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("임시 파일 삭제 실패: {}", path, e);
        }
    }
}
