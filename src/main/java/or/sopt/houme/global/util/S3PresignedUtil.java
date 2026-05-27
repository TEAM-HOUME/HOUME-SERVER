package or.sopt.houme.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;



@Component
@Slf4j
@RequiredArgsConstructor
public class S3PresignedUtil {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.bucket-domain}")
    private String bucketDomain;


    /**
     * S3에 이미지 업로드를 위한 PreSigned URL을 생성하는 메서드입니다.
     *
     * 로직 흐름:
     * 1. 이미지 확장자 유효성을 검증합니다.
     * 2. 디렉토리명을 정제(sanitize)하여 S3 키에 사용할 수 있도록 처리합니다.
     * 3. 고유한 키(S3 object key)를 생성합니다.
     * 4. S3Presigner를 사용하여 업로드용 PreSigned URL을 생성합니다.
     * 5. 생성된 PreSigned URL과 공개 접근 가능한 이미지 URL, 키 정보를 포함한 DTO를 반환합니다.
     *
     * @param imageExtension 업로드할 이미지 파일의 확장자
     * @param dirName 저장할 S3 디렉토리 이름
     * @param contentType 업로드할 파일의 Content-Type
     *
     * @return 업로드 URL, 공개 URL, 키 등을 포함한 응답 DTO
     *
     * @throws GeneralException 지원하지 않는 이미지 확장자인 경우 예외 발생
     */
    public S3PresignedUrlResponseDTO createPresignedUrl(String imageExtension, String dirName, String contentType) {

        if (!isValidImageExtension(imageExtension)) {
            log.warn("event=s3.presigned_url.invalid_extension imageExtension={} dirName={}", imageExtension, dirName);
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        }

        String sanitizedDirName = sanitizeDirectoryName(dirName);
        log.debug("디렉토리명 정제 완료: {} -> {}", dirName, sanitizedDirName);

        String keyName = generateUniqueKeyName(imageExtension, sanitizedDirName);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(keyName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // 보안을 위해 짧은 유효시간 설정
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        String uploadUrl = presignedRequest.url().toString();

        String publicUrl = bucketDomain + "/" + keyName;

        log.info("event=s3.presigned_url.created key={} directory={} contentType={}", keyName, sanitizedDirName, contentType);

        return new S3PresignedUrlResponseDTO(
                uploadUrl,           // 클라이언트 업로드용 URL
                publicUrl,           // 데이터베이스 저장용 공개 URL
                keyName,             // 파일 관리용 S3 키
                sanitizedDirName     // 정제된 디렉토리명
        );
    }


    /**
     * 디렉토리명을 검증하고 정제하는 메서드 입니다
     */
    private String sanitizeDirectoryName(String dirName) {
        // null이나 빈 문자열인 경우 기본값 반환
        if (dirName == null || dirName.trim().isEmpty()) {
            return "images";
        }

        // 특수문자 제거 및 소문자 변환 (영문, 숫자, 하이픈, 언더스코어만 허용)
        String sanitized = dirName.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "")
                .replaceAll("_{2,}", "_")  // 연속된 언더스코어 제거
                .replaceAll("-{2,}", "-"); // 연속된 하이픈 제거

        // 빈 문자열이 되었다면 기본값 반환
        if (sanitized.isEmpty()) {
            return "images";
        }

        // 길이 제한 (최대 50자)
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }

        return sanitized;
    }

    /**
     * 디렉토리명을 포함한 고유한 키 이름 생성
     *
     * @param extension 파일 확장자
     * @param dirName 디렉토리명
     * @return 고유한 S3 키명
     */
    private String generateUniqueKeyName(String extension, String dirName) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());

        // 디렉토리명/타임스탬프_UUID축약.확장자 형식
        return String.format("%s/%s_%s.%s",
                dirName,
                timestamp,
                uuid.substring(0, 8),
                extension.toLowerCase());
    }

    /**
     * 고유한 키 이름 생성
     */
    private String generateUniqueKeyName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return String.format("images/%s_%s.%s", timestamp, uuid.substring(0, 8), extension.toLowerCase());
    }

    /**
     * 이미지 확장자 검증
     */
    private boolean isValidImageExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }

        String lowerExt = extension.toLowerCase();
        return lowerExt.equals("jpg") ||
                lowerExt.equals("jpeg") ||
                lowerExt.equals("png") ||
                lowerExt.equals("gif") ||
                lowerExt.equals("webp");
    }
}
