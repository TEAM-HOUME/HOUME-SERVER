package or.sopt.houme.ai.imagehash;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import or.sopt.houme.domain.furniture.client.FastApiImageHashClient;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.ImageHashRequest;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.SimilarityResponse;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.forPlan.ImageHashRequestForPlan;
import or.sopt.houme.domain.furniture.dto.external.fastApiImagehash.forPlan.SimilarityResponseForPlan;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FastAPI 이미지 해시 유사도 API 대체 구현.
 *
 * - pHash(형태) 70% + colorHash(색상) 30% 가중치 기반 유사도 계산
 * - Top-5 상품 반환
 * - for-plan 변형은 외부 가중치 비율을 정규화하여 반영
 *
 * 구현 세부
 * - pHash: 32x32 그레이스케일 → 2D DCT → 상위 8x8 블록의 중앙값 기반 64비트 해시
 * - colorHash: HSV Hue 64-bin 히스토그램 → 평균 이상인 bin을 1로 표기한 64비트 해시
 * - 유사도: 1 - (해밍거리 / 64)
 */
@Component
@Primary
@RequiredArgsConstructor
public class LocalFastApiImageHashClient implements FastApiImageHashClient {

    private static final double PHASH_WEIGHT = 0.7;
    private static final double COLOR_HASH_WEIGHT = 0.3;
    private static final int TOP_K = 5;
    private static final int PHASH_SIZE = 32; // DCT 입력 크기
    private static final int PHASH_REDUCED_SIZE = 8; // 상위 블록 크기

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * 기본 가중치(0.7/0.3) 기반 유사도 상위 5개 상품을 반환합니다.
     */
    @Override
    public SimilarityResponse getTopKSimilarImages(ImageHashRequest request) {
        try {
            BufferedImage base = fetchImage(request.baseImageUrl());
            long baseP = computePHash(base);
            long baseC = computeColorHash(base);

            List<SimilarityResponse.RankedProduct> ranked = new ArrayList<>();
            for (ImageHashRequest.Product p : request.products()) {
                try {
                    BufferedImage img = fetchImage(p.imageUrl());
                    long pP = computePHash(img);
                    long pC = computeColorHash(img);

                    double sim = weightedSimilarity(baseP, baseC, pP, pC, PHASH_WEIGHT, COLOR_HASH_WEIGHT);
                    ranked.add(SimilarityResponse.RankedProduct.of(p.productId(), p.imageUrl(), round4(sim)));
                } catch (Exception e) {
                    // 개별 실패는 0점 처리 후 계속 진행
                    ranked.add(SimilarityResponse.RankedProduct.of(p.productId(), p.imageUrl(), 0.0));
                }
            }

            List<SimilarityResponse.RankedProduct> top = ranked.stream()
                    .sorted(Comparator.comparingDouble(SimilarityResponse.RankedProduct::similarity).reversed())
                    .limit(TOP_K)
                    .collect(Collectors.toList());

            return SimilarityResponse.of(top);
        } catch (Exception e) {
            // 전체 실패 시 빈 결과 반환 (서비스 계층에서 예외 처리)
            return SimilarityResponse.of(List.of());
        }
    }

    /**
     * 외부 가중치 pHash/colorHash(0~100)를 정규화하여 유사도 계산을 수행합니다.
     */
    @Override
    public SimilarityResponseForPlan getTopKSimilarImagesForPlan(ImageHashRequestForPlan request) {
        try {
            BufferedImage base = fetchImage(request.baseImageUrl());
            long baseP = computePHash(base);
            long baseC = computeColorHash(base);

            int p = Math.max(0, request.pHash());
            int c = Math.max(0, request.colorHash());
            int total = p + c;
            double pRatio, cRatio;
            if (total <= 0) {
                pRatio = PHASH_WEIGHT;
                cRatio = COLOR_HASH_WEIGHT;
            } else {
                pRatio = Math.min(1.0, (double) p / total);
                cRatio = Math.min(1.0, (double) c / total);
            }

            List<SimilarityResponseForPlan.RankedProduct> ranked = new ArrayList<>();
            for (ImageHashRequestForPlan.Product prod : request.products()) {
                try {
                    BufferedImage img = fetchImage(prod.imageUrl());
                    long pP = computePHash(img);
                    long pC = computeColorHash(img);

                    double sim = weightedSimilarity(baseP, baseC, pP, pC, pRatio, cRatio);
                    ranked.add(new SimilarityResponseForPlan.RankedProduct(prod.productId(), prod.imageUrl(), round4(sim)));
                } catch (Exception e) {
                    ranked.add(new SimilarityResponseForPlan.RankedProduct(prod.productId(), prod.imageUrl(), 0.0));
                }
            }

            List<SimilarityResponseForPlan.RankedProduct> top = ranked.stream()
                    .sorted(Comparator.comparingDouble(SimilarityResponseForPlan.RankedProduct::similarity).reversed())
                    .limit(TOP_K)
                    .collect(Collectors.toList());

            return SimilarityResponseForPlan.of(top);
        } catch (Exception e) {
            return SimilarityResponseForPlan.of(List.of());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 내부 유틸 구현부
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 이미지 다운로드 유틸 (타임아웃/에러 처리 포함)
     */
    private BufferedImage fetchImage(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .header("User-Agent", "Houme-ImageHash/1.0")
                .build();

        HttpResponse<byte[]> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Image fetch failed: status=" + resp.statusCode() + ", url=" + url);
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(resp.body())) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new IOException("Unsupported image format: " + url);
            }
            return ensureRgb(img);
        }
    }

    private BufferedImage ensureRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) return src;
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgb;
    }

    /**
     * pHash 계산: 32x32 그레이스케일 → 2D DCT → 8x8 상위 블록 중앙값 기준 64-bit 해시
     */
    private long computePHash(BufferedImage image) {
        double[][] gray = toGrayscaleMatrix(resize(image, PHASH_SIZE, PHASH_SIZE));
        double[][] dct = dct2D(gray);

        // 상위 8x8 블록만 사용
        double[] vals = new double[PHASH_REDUCED_SIZE * PHASH_REDUCED_SIZE];
        int idx = 0;
        for (int u = 0; u < PHASH_REDUCED_SIZE; u++) {
            for (int v = 0; v < PHASH_REDUCED_SIZE; v++) {
                vals[idx++] = dct[u][v];
            }
        }
        double median = median(vals);

        // 64비트 해시 구성 (상위비트부터 채움)
        long hash = 0L;
        for (double v : vals) {
            hash <<= 1;
            if (v > median) hash |= 1L;
        }
        return hash;
    }

    /**
     * colorHash 계산: HSV Hue 64-bin 히스토그램 기반 64-bit 해시
     */
    private long computeColorHash(BufferedImage image) {
        BufferedImage small = resize(image, 64, 64);
        int width = small.getWidth();
        int height = small.getHeight();
        int[] hist = new int[64];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = small.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                float[] hsb = Color.RGBtoHSB(r, g, b, null);
                // Hue ∈ [0,1) → 64개 bin
                int bin = (int) Math.floor(hsb[0] * 64.0f);
                if (bin < 0) bin = 0; if (bin > 63) bin = 63;
                hist[bin]++;
            }
        }

        double avg = 0.0;
        for (int v : hist) avg += v;
        avg /= hist.length;

        long hash = 0L;
        for (int v : hist) {
            hash <<= 1;
            if (v >= avg) hash |= 1L;
        }
        return hash;
    }

    private double weightedSimilarity(long baseP, long baseC, long pP, long pC, double wP, double wC) {
        double sP = hashSimilarity(baseP, pP);
        double sC = hashSimilarity(baseC, pC);
        return wP * sP + wC * sC;
    }

    /** 64비트 해시 간 유사도 (1 - 해밍거리/64) */
    private double hashSimilarity(long h1, long h2) {
        int dist = Long.bitCount(h1 ^ h2);
        return 1.0 - (dist / 64.0);
    }

    private double[][] toGrayscaleMatrix(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        double[][] m = new double[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // 표준 가중치 그레이스케일 변환
                m[x][y] = 0.299 * r + 0.587 * g + 0.114 * b;
            }
        }
        return m;
    }

    private BufferedImage resize(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    /** 간단한 2D DCT 구현 */
    private double[][] dct2D(double[][] input) {
        int N = input.length; // 가정: 정사각형
        double[][] output = new double[N][N];

        double c0 = 1.0 / Math.sqrt(N);
        double c = Math.sqrt(2.0) / Math.sqrt(N);

        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                double sum = 0.0;
                for (int x = 0; x < N; x++) {
                    for (int y = 0; y < N; y++) {
                        sum += input[x][y]
                                * Math.cos(((2 * x + 1) * u * Math.PI) / (2.0 * N))
                                * Math.cos(((2 * y + 1) * v * Math.PI) / (2.0 * N));
                    }
                }
                double au = (u == 0) ? c0 : c;
                double av = (v == 0) ? c0 : c;
                output[u][v] = au * av * sum;
            }
        }
        return output;
    }

    private double median(double[] arr) {
        double[] copy = arr.clone();
        java.util.Arrays.sort(copy);
        int n = copy.length;
        if (n % 2 == 1) return copy[n / 2];
        return (copy[n / 2 - 1] + copy[n / 2]) / 2.0;
    }

    private double round4(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}

