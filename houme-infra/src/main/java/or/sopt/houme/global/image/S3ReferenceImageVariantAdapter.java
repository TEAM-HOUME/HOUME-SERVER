package or.sopt.houme.global.image;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.port.out.ReferenceImageVariantPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** 기존 image sweep이 S3에 생성한 관리자 이미지 WebP variant를 Gemini 입력에 재사용한다. */
@Component
@Slf4j
public class S3ReferenceImageVariantAdapter implements ReferenceImageVariantPort {

    private static final Set<String> TARGET_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final String AWS_DOMAIN_SUFFIX = ".amazonaws.com";
    private static final List<String> ADMIN_IMAGE_PREFIXES = List.of(
            "floorplan/", "furniture/", "moodboard/", "banner/", "style/", "landing/"
    );

    private final AmazonS3 amazonS3;
    private final VariantKeyResolver variantKeyResolver;
    private final String bucket;
    private final int variantWidth;

    public S3ReferenceImageVariantAdapter(
            AmazonS3 amazonS3,
            VariantKeyResolver variantKeyResolver,
            @Value("${cloud.aws.s3.bucket}") String bucket,
            @Value("${image.gemini.variant-width:1280}") int variantWidth
    ) {
        this.amazonS3 = amazonS3;
        this.variantKeyResolver = variantKeyResolver;
        this.bucket = bucket;
        this.variantWidth = variantWidth;
    }

    @Override
    public Optional<String> findVariantUrl(String originalUrl) {
        Optional<String> originalKey = extractManagedOriginalKey(originalUrl);
        if (originalKey.isEmpty()) {
            return Optional.empty();
        }

        String variantKey = variantKeyResolver.toVariantKey(originalKey.get(), variantWidth);
        try {
            if (!amazonS3.doesObjectExist(bucket, variantKey)) {
                return Optional.empty();
            }
            return Optional.of(amazonS3.getUrl(bucket, variantKey).toString());
        } catch (SdkClientException e) {
            // Variant 조회 실패가 이미지 생성을 막으면 안 되므로, 호출 측은 원본 런타임 압축으로 폴백한다.
            log.warn("Gemini 참고 이미지 variant 조회 실패. 원본으로 폴백합니다. key={}", variantKey, e);
            return Optional.empty();
        }
    }

    private Optional<String> extractManagedOriginalKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(imageUrl);
            String host = uri.getHost();
            String path = trimLeadingSlash(uri.getPath());
            if (host == null || path.isBlank()) {
                return Optional.empty();
            }

            String key = extractKey(host.toLowerCase(Locale.ROOT), path);
            if (!isSweepTargetOriginal(key)) {
                return Optional.empty();
            }
            return Optional.of(key);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String extractKey(String host, String path) {
        String virtualHostedPrefix = bucket.toLowerCase(Locale.ROOT) + ".s3";
        if (host.startsWith(virtualHostedPrefix + ".") && host.endsWith(AWS_DOMAIN_SUFFIX)) {
            return path;
        }
        if (host.startsWith("s3.") && host.endsWith(AWS_DOMAIN_SUFFIX)) {
            int separator = path.indexOf('/');
            if (separator > 0 && bucket.equals(path.substring(0, separator))) {
                return path.substring(separator + 1);
            }
        }
        return "";
    }

    private boolean isSweepTargetOriginal(String key) {
        if (key.isBlank() || variantKeyResolver.isVariantKey(key)
                || ADMIN_IMAGE_PREFIXES.stream().noneMatch(key::startsWith)) {
            return false;
        }
        int lastDot = key.lastIndexOf('.');
        return lastDot >= 0 && TARGET_EXTENSIONS.contains(key.substring(lastDot + 1).toLowerCase(Locale.ROOT));
    }

    private String trimLeadingSlash(String path) {
        if (path == null) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
