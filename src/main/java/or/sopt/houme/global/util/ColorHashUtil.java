package or.sopt.houme.global.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.net.URL;
import java.util.Arrays;

public class ColorHashUtil {
    private static final int BINS = 8; // Hue, Saturation, Value 각각 8구간 → 512 차원

    public static double[] getColorHistogram(BufferedImage img) {
        int[] hist = new int[BINS * BINS * BINS];
        int w = img.getWidth();
        int h = img.getHeight();

        Raster raster = img.getRaster();
        int[] pixels = new int[w * 3]; // 한 줄의 RGB 값 (R,G,B,R,G,B,...)

        for (int y = 0; y < h; y++) {
            // 사진의 y번째 줄의 픽셀이 나뉘어, pixels라는 배열에 저장됨
            raster.getPixels(0, y, w, 1, pixels);
            for (int i = 0; i < pixels.length; i += 3) {
                int r = pixels[i];
                int g = pixels[i + 1];
                int b = pixels[i + 2];

                // 해당 픽셀의 rgp -> hsb로 바꿈, hsb는 사람이 직관적으로 인식할 수 있는 색상 표시이기 때문에
                float[] hsv = Color.RGBtoHSB(r, g, b, null);

                // 실수값들을 8등분으로 나눔
                int hi = Math.min(BINS - 1, (int) (hsv[0] * BINS));
                int si = Math.min(BINS - 1, (int) (hsv[1] * BINS));
                int vi = Math.min(BINS - 1, (int) (hsv[2] * BINS));

                // 해당하는 히스토그램 인덱스에 1 더하기
                int idx = hi * BINS * BINS + si * BINS + vi;
                hist[idx]++;
            }
        }

        // 이미지의 각 픽셀 1개당 히스토그램의 1
        // 히스토그램의 값들을 0~1사이의 값으로 확률분포화 시킴
        double sum = Arrays.stream(hist).sum();
        return Arrays.stream(hist).mapToDouble(v -> v / sum).toArray();
    }

    public static double[] getColorHistogramFromUrl(String imageUrl) throws Exception {
        BufferedImage img = ImageIO.read(new URL(imageUrl));
        return getColorHistogram(img);
    }

    // 두 벡터(색상 히스토그램 등)의 코사인 유사도(cosine similarity)를 계산한다.
    public static double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;

        // 두 벡터를 순회하면서 내적과 각 벡터의 크기를 동시에 계산
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        // 코사인 유사도 계산
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-8);
    }
}
