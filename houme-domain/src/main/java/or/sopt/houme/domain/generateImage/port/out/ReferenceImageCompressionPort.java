package or.sopt.houme.domain.generateImage.port.out;

import or.sopt.houme.domain.generateImage.model.ReferenceImageCompressionResult;

import java.nio.file.Path;

public interface ReferenceImageCompressionPort {

    ReferenceImageCompressionResult compressForGemini(Path source, long sourceBytes);
}
