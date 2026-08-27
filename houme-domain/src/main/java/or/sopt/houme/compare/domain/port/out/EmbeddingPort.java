package or.sopt.houme.compare.domain.port.out;

import java.util.List;

public interface EmbeddingPort {
    List<Double> embedText(String text);
    List<Double> embedImageUrl(String imageUrl);
    String downloadBase64(String url);
}
