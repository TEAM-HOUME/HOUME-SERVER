package or.sopt.houme.global.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.S3Exception;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3UtilImpl implements S3Util {


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;


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
        metadata.setContentType(file.getContentType());

        String originalFileName = file.getOriginalFilename();

        try {

            // 이미지를 저장합니다
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

        } catch (AmazonServiceException e) {
            throw new S3Exception(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        } catch (IOException e) {
            throw new S3Exception(ErrorCode.IMAGE_UPLOAD_IO_EXCEPTION);
        }

        // 저장된 링크를 포함한 메타데이터를 반환합니다. 메타데이터는 추후 논의하고 어떤 데이터를 넣을지 얘기해보는게 좋을 것 같습니다
        return ImageUploadResponseDTO.from(fileName,originalFileName,amazonS3.getUrl(bucket, fileName).toString());
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
            throw new S3Exception(ErrorCode.IMAGE_DELETE_EXCEPTION);
        }
    }
}
