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


    public S3PresignedUrlResponseDTO createPresignedUrl(String imageExtension, String dirName, String contentType) {

        // 1. 입력 검증: 지원 가능한 이미지 확장자인지 확인
        // jpg, jpeg, png, gif, webp만 허용하며, 대소문자 구분하지 않음
        if (!isValidImageExtension(imageExtension)) {
            log.warn("지원하지 않는 이미지 확장자 요청: {}", imageExtension);
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        }

        // 2. 디렉토리명 검증 및 정제
        // null이거나 빈 문자열인 경우 기본값 "images" 사용
        // 특수문자 제거 및 안전한 디렉토리명으로 변환
        String sanitizedDirName = sanitizeDirectoryName(dirName);
        log.debug("디렉토리명 정제 완료: {} -> {}", dirName, sanitizedDirName);

        // 3. S3 객체 키 생성: 디렉토리명을 포함하여 중복을 방지하기 위해 타임스탬프와 UUID를 조합
        // 형식: {dirName}/{timestamp}_{uuid}.{extension}
        String keyName = generateUniqueKeyName(imageExtension, sanitizedDirName);

        // 5. S3 PutObject 요청 객체 생성
        // - bucket: 파일이 저장될 S3 버킷명
        // - key: S3 내에서 객체를 식별하는 고유 키 (디렉토리 포함)
        // - contentType: 파일의 MIME 타입 (브라우저 렌더링에 필요)
        // - metadata: 추가 메타데이터 (파일 관리 및 검색에 활용)
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(keyName)
                .build();

        // 6. Presigned URL 요청 생성
        // - signatureDuration: URL 유효 시간 (10분)
        // - putObjectRequest: 실제 업로드할 때 사용될 요청 정보
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // 보안을 위해 짧은 유효시간 설정
                .putObjectRequest(objectRequest)
                .build();

        // 7. AWS S3 Presigner를 통해 실제 Presigned URL 생성
        // 이 URL은 AWS 서명이 포함된 임시 URL로, 지정된 시간 동안만 유효함
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        // 8. 클라이언트가 사용할 업로드 URL 추출
        String uploadUrl = presignedRequest.url().toString();

        // 9. 업로드 완료 후 접근할 수 있는 공개 URL 생성
        // bucketDomain은 CDN이나 커스텀 도메인일 수 있음
        String publicUrl = bucketDomain + "/" + keyName;

        // 10. 디버깅 및 모니터링을 위한 로그 기록
        log.info("Generated presigned URL for key: {}, directory: {}", keyName, sanitizedDirName);
        log.debug("Upload URL: {}", uploadUrl);  // 민감한 정보이므로 DEBUG 레벨로 기록

        // 11. DTO로 결과 반환
        // Record를 사용하여 타입 안전성과 불변성 보장
        return new S3PresignedUrlResponseDTO(
                uploadUrl,           // 클라이언트 업로드용 URL
                publicUrl,           // 데이터베이스 저장용 공개 URL
                keyName,             // 파일 관리용 S3 키
                sanitizedDirName     // 정제된 디렉토리명
        );
    }

    /**
     * 디렉토리명을 검증하고 정제하는 메서드
     *
     * @param dirName 원본 디렉토리명
     * @return 정제된 안전한 디렉토리명
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