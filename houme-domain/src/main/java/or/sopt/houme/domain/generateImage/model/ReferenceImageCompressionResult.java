package or.sopt.houme.domain.generateImage.model;

/** Gemini 참고 이미지 압축 결과. 압축이 이득이 없거나 실패하면 원본 바이트를 유지한다. */
public record ReferenceImageCompressionResult(
        byte[] bytes,
        boolean compressed,
        long compressionMillis
) {

    public static ReferenceImageCompressionResult original(byte[] source, long compressionMillis) {
        return new ReferenceImageCompressionResult(source, false, compressionMillis);
    }
}
