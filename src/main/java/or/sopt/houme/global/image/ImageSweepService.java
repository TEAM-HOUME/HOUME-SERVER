package or.sopt.houme.global.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.util.S3Util;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 어드민 이미지 폴더(S3 prefix)를 훑어 variant가 없는 원본을 찾아 리사이즈+WebP 변환합니다.
 *
 * - 멱등: 이미 모든 너비의 variant가 있으면 건너뜁니다.
 * - 예외 격리: prefix 단위/이미지 단위로 try-catch해 일부 실패가 전체를 중단시키지 않습니다.
 * - 원본 불변: 변환 실패 시 원본은 그대로 두고 해당 이미지만 건너뜁니다(프론트는 원본으로 폴백).
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageSweepService {

    /** 어드민이 업로드하는 이미지가 저장되는 S3 prefix들 (AI 생성/상품 이미지는 포함되지 않음) */
    private static final List<String> ADMIN_IMAGE_PREFIXES = List.of(
            "floorplan/", "furniture/", "moodboard/", "banner/", "style/", "landing/"
    );

    /** 변환 대상 원본 확장자 (이미 webp인 이미지나 variant는 제외) */
    private static final Set<String> TARGET_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final S3Util s3Util;
    private final ImageOptimizer imageOptimizer;
    private final VariantKeyResolver variantKeyResolver;

    /**
     * 모든 어드민 prefix를 훑어 variant가 없는 원본을 변환합니다.
     */
    public void sweep() {
        log.info("이미지 최적화 sweep 시작");
        int convertedTotal = 0;
        for (String prefix : ADMIN_IMAGE_PREFIXES) {
            try {
                convertedTotal += sweepPrefix(prefix);
            } catch (Exception e) {
                log.error("prefix sweep 실패: {} — 건너뜀", prefix, e);
            }
        }
        log.info("이미지 최적화 sweep 종료. 신규 변환 원본 수={}", convertedTotal);
    }

    private int sweepPrefix(String prefix) {
        List<String> keys = s3Util.listKeys(prefix);
        // 한 번의 목록 조회로 원본 + variant를 모두 받아 메모리에서 존재 여부를 비교 (객체별 추가 조회 불필요)
        Set<String> existingKeys = new HashSet<>(keys);

        List<String> originals = keys.stream()
                .filter(key -> !variantKeyResolver.isVariantKey(key))
                .filter(this::isTargetOriginal)
                .toList();

        int converted = 0;
        for (String originalKey : originals) {
            boolean allVariantsExist = variantKeyResolver.variantKeysFor(originalKey).stream()
                    .allMatch(existingKeys::contains);
            if (allVariantsExist) {
                continue;
            }
            try {
                convertMissingVariants(originalKey, existingKeys);
                converted++;
            } catch (Exception e) {
                log.error("원본 변환 실패: {} — 원본 보존 후 건너뜀", originalKey, e);
            }
        }
        return converted;
    }

    private void convertMissingVariants(String originalKey, Set<String> existingKeys) {
        byte[] source = s3Util.download(originalKey);
        for (int width : VariantKeyResolver.VARIANT_WIDTHS) {
            String variantKey = variantKeyResolver.toVariantKey(originalKey, width);
            if (existingKeys.contains(variantKey)) {
                continue;
            }
            byte[] webp = imageOptimizer.toResizedWebp(source, width);
            s3Util.uploadWebpVariant(variantKey, webp);
            log.info("variant 생성: {} ({} bytes)", variantKey, webp.length);
        }
    }

    private boolean isTargetOriginal(String key) {
        int lastDot = key.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        }
        String extension = key.substring(lastDot + 1).toLowerCase();
        return TARGET_EXTENSIONS.contains(extension);
    }
}
