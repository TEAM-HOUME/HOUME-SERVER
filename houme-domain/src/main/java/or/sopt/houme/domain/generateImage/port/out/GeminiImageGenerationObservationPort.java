package or.sopt.houme.domain.generateImage.port.out;

import or.sopt.houme.domain.generateImage.model.GeminiImageGenerationObservation;

public interface GeminiImageGenerationObservationPort {

    void save(GeminiImageGenerationObservation observation);
}
