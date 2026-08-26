package or.sopt.houme.compare.application.filter;

import org.springframework.stereotype.Component;

@Component
public class PriceSoftFilter {

    private static final double LOG_MIN = Math.log10(30_000);
    private static final double LOG_MAX = Math.log10(8_000_000);

    /**
     * @param originalKrw  원본 상품 가격 (KRW). null이면 항상 통과.
     * @param candidateKrw 비교 상품 가격 (KRW)
     */
    public boolean passes(Double originalKrw, double candidateKrw) {
        if (originalKrw == null) return true;

        double t = clamp((Math.log10(originalKrw) - LOG_MIN) / (LOG_MAX - LOG_MIN), 0, 1);
        double fLow  = 0.8 - (0.8 - 0.6) * t;
        double fHigh = 0.5 - (0.5 - 0.15) * t;

        double minPrice = originalKrw * (1 - fLow);
        double maxPrice = originalKrw * (1 + fHigh);

        return candidateKrw >= minPrice && candidateKrw <= maxPrice;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
