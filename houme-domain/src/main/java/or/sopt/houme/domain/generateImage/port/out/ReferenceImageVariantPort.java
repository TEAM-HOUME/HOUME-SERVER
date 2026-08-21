package or.sopt.houme.domain.generateImage.port.out;

import java.util.Optional;

/** 원본 참고 이미지 URL에 대응하는 사전 생성 WebP variant를 조회한다. */
public interface ReferenceImageVariantPort {

    Optional<String> findVariantUrl(String originalUrl);
}
