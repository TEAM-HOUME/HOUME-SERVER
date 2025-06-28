package or.sopt.houme.global.util;

import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface S3Util {
    void upload(byte[] image, String dirname);
    ImageUploadResponseDTO upload(String dirName, MultipartFile file);

    void delete(String fileUrl);
}
