package or.sopt.houme.global.image;

/**
 * 이미지 최적화(리사이즈/WebP 변환) 과정에서 발생하는 예외
 */
public class ImageOptimizationException extends RuntimeException {

    public ImageOptimizationException(String message) {
        super(message);
    }

    public ImageOptimizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
