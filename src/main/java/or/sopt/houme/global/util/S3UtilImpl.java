package or.sopt.houme.global.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.S3Exception;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.constant.S3ExtensionConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static or.sopt.houme.global.util.constant.S3ExtensionConstant.EXTENSION_PNG;
import static or.sopt.houme.global.util.constant.S3ExtensionConstant.EXTENSION_WEBP;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3UtilImpl implements S3Util {


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    private static final String CONTENT_TYPE_WEBP = "image/webp";
    // 최적화 이미지(variant)는 내용이 바뀌지 않으므로 장기 캐싱 (원본은 Cache-Control 미설정 → 재방문 시 재다운로드 발생)
    private static final String CACHE_CONTROL_IMMUTABLE = "public, max-age=31536000, immutable";


    /**
     * 이미지를 저장하는 유틸 메서드입니다
     *
     * @param dirName S3 내부에 파일을 저장할 위치를 정의합니다
     * @param file 저장할 파일을 정의합니다. 이때 타입은 MulitpartFile 입니다
     * */
    @Override
    public ImageUploadResponseDTO upload(String dirName, MultipartFile file) {

        // 이미지를 다루는 key의 역할을 하는 필드입니다
        String fileName = dirName + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        // S3 이미지 저장에 필요한 메타데이터를 정의합니다
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        String contentType = file.getContentType();
        metadata.setContentType(contentType);

        String originalFileName = file.getOriginalFilename();

        try {

            // 이미지를 저장합니다
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

        } catch (AmazonServiceException e) {
            log.error(
                    "S3 upload failed (service). bucket={}, key={}, statusCode={}, errorCode={}, requestId={}, message={}",
                    bucket,
                    fileName,
                    e.getStatusCode(),
                    e.getErrorCode(),
                    e.getRequestId(),
                    e.getErrorMessage(),
                    e
            );
            throw new S3Exception(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        } catch (SdkClientException e) {
            log.error(
                    "S3 upload failed (client). bucket={}, key={}, message={}",
                    bucket,
                    fileName,
                    e.getMessage(),
                    e
            );
            throw new S3Exception(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        } catch (IOException e) {
            log.error(
                    "S3 upload failed (io). bucket={}, key={}, message={}",
                    bucket,
                    fileName,
                    e.getMessage(),
                    e
            );
            throw new S3Exception(ErrorCode.IMAGE_UPLOAD_IO_EXCEPTION);
        }

        // 저장된 링크를 포함한 메타데이터를 반환합니다. 메타데이터는 추후 논의하고 어떤 데이터를 넣을지 얘기해보는게 좋을 것 같습니다
        return ImageUploadResponseDTO.from(fileName,originalFileName,amazonS3.getUrl(bucket, fileName).toString(),contentType);
    }


    /**
     * byte[] 로 반환된 이미지를 저장하는 메서드입니다.
     *
     * @param dirName S3 내부에 파일을 저장할 위치를 정의합니다
     * @param imageBytes 저장할 파일을 정의합니다. 이때 타입은 byte[] 입니다
     * */
    @Override
    public ImageUploadResponseDTO uploadByByte(String dirName, byte[] imageBytes) {

        String originalFileName = UUID.randomUUID() + EXTENSION_WEBP;
        String contentType = "image/webp";

        String fileName = dirName + "/" + UUID.randomUUID() + "-" + originalFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageBytes.length);
        metadata.setContentType(contentType);

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));
        } catch (AmazonServiceException e) {
            log.error(
                    "S3 uploadByByte failed (service). bucket={}, key={}, statusCode={}, errorCode={}, requestId={}, message={}",
                    bucket,
                    fileName,
                    e.getStatusCode(),
                    e.getErrorCode(),
                    e.getRequestId(),
                    e.getErrorMessage(),
                    e
            );
            throw new S3Exception(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        } catch (SdkClientException e) {
            log.error(
                    "S3 uploadByByte failed (client). bucket={}, key={}, message={}",
                    bucket,
                    fileName,
                    e.getMessage(),
                    e
            );
            throw new S3Exception(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        } catch (Exception e) {
            log.error(
                    "S3 uploadByByte failed (other). bucket={}, key={}, message={}",
                    bucket,
                    fileName,
                    e.getMessage(),
                    e
            );
            throw new S3Exception(ErrorCode.IMAGE_UPLOAD_IO_EXCEPTION);
        }

        return ImageUploadResponseDTO.from(
                fileName,
                originalFileName,
                amazonS3.getUrl(bucket, fileName).toString(),
                contentType
        );
    }




    /**
     * S3에서 key에 해당하는 객체를 바이트로 다운로드합니다.
     *
     * @param key S3 객체 key
     * @return 객체 바이트
     */
    @Override
    public byte[] download(String key) {
        try (S3Object s3Object = amazonS3.getObject(bucket, key);
             InputStream content = s3Object.getObjectContent()) {
            return content.readAllBytes();
        } catch (AmazonServiceException e) {
            log.error(
                    "S3 download failed (service). bucket={}, key={}, statusCode={}, errorCode={}, requestId={}, message={}",
                    bucket, key, e.getStatusCode(), e.getErrorCode(), e.getRequestId(), e.getErrorMessage(), e
            );
            throw new S3Exception(ErrorCode.IMAGE_DOWNLOAD_EXCEPTION);
        } catch (SdkClientException e) {
            log.error("S3 download failed (client). bucket={}, key={}, message={}", bucket, key, e.getMessage(), e);
            throw new S3Exception(ErrorCode.IMAGE_DOWNLOAD_EXCEPTION);
        } catch (IOException e) {
            log.error("S3 download failed (io). bucket={}, key={}, message={}", bucket, key, e.getMessage(), e);
            throw new S3Exception(ErrorCode.IMAGE_DOWNLOAD_EXCEPTION);
        }
    }

    /**
     * WebP 변환본(variant)을 지정한 key에 업로드합니다.
     * content-type은 image/webp, Cache-Control은 장기 캐싱(immutable)으로 설정합니다.
     *
     * @param key       variant를 저장할 S3 key (네이밍 규칙으로 결정된 key)
     * @param webpBytes WebP 변환 결과 바이트
     */
    @Override
    public void uploadWebpVariant(String key, byte[] webpBytes) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(webpBytes.length);
        metadata.setContentType(CONTENT_TYPE_WEBP);
        metadata.setCacheControl(CACHE_CONTROL_IMMUTABLE);

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(webpBytes);
            amazonS3.putObject(new PutObjectRequest(bucket, key, inputStream, metadata));
        } catch (AmazonServiceException e) {
            log.error(
                    "S3 variant upload failed (service). bucket={}, key={}, statusCode={}, errorCode={}, requestId={}, message={}",
                    bucket, key, e.getStatusCode(), e.getErrorCode(), e.getRequestId(), e.getErrorMessage(), e
            );
            throw new S3Exception(ErrorCode.IMAGE_VARIANT_UPLOAD_EXCEPTION);
        } catch (SdkClientException e) {
            log.error("S3 variant upload failed (client). bucket={}, key={}, message={}", bucket, key, e.getMessage(), e);
            throw new S3Exception(ErrorCode.IMAGE_VARIANT_UPLOAD_EXCEPTION);
        }
    }

    /**
     * S3에서, 지정한 prefix 아래의 모든 객체 key를 반환합니다.
     *
     * @param prefix S3 prefix (예: "floorplan/")
     * @return prefix 아래 객체 key 목록
     */
    @Override
    public List<String> listKeys(String prefix) {
        List<String> keys = new ArrayList<>();
        try {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucket)
                    .withPrefix(prefix);
            ListObjectsV2Result result;
            do {
                result = amazonS3.listObjectsV2(request);
                for (S3ObjectSummary summary : result.getObjectSummaries()) {
                    keys.add(summary.getKey());
                }
                request.setContinuationToken(result.getNextContinuationToken());
            } while (result.isTruncated());
        } catch (AmazonServiceException e) {
            log.error(
                    "S3 list failed (service). bucket={}, prefix={}, statusCode={}, errorCode={}, requestId={}, message={}",
                    bucket, prefix, e.getStatusCode(), e.getErrorCode(), e.getRequestId(), e.getErrorMessage(), e
            );
            throw new S3Exception(ErrorCode.IMAGE_LIST_EXCEPTION);
        } catch (SdkClientException e) {
            log.error("S3 list failed (client). bucket={}, prefix={}, message={}", bucket, prefix, e.getMessage(), e);
            throw new S3Exception(ErrorCode.IMAGE_LIST_EXCEPTION);
        }
        return keys;
    }

    /**
     * key 기반으로 이미지를 삭제하는 메서드입니다.
     *
     * @param filename S3에 저장된 객체의 key (예: "test/uuid-filename.png")
     */
    @Override
    public void delete(String filename) {
        try {
            amazonS3.deleteObject(bucket, filename);

            if (amazonS3.doesObjectExist(bucket, filename)) {
                throw new S3Exception(ErrorCode.IMAGE_STILL_EXIST);
            }

        } catch (AmazonServiceException e) {
            log.error(
                    "S3 delete failed. bucket={}, key={}, statusCode={}, errorCode={}, requestId={}, message={}",
                    bucket,
                    filename,
                    e.getStatusCode(),
                    e.getErrorCode(),
                    e.getRequestId(),
                    e.getErrorMessage(),
                    e
            );
            throw new S3Exception(ErrorCode.IMAGE_DELETE_EXCEPTION);
        }
    }
}
