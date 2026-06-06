package or.sopt.houme.global.util;

import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Util {

    ImageUploadResponseDTO upload(String dirName, MultipartFile file);

    ImageUploadResponseDTO uploadByByte(String dirName, byte[] imageBytes);

    byte[] download(String key);

    void uploadWebpVariant(String key, byte[] webpBytes);

    List<String> listKeys(String prefix);

    void delete(String fileUrl);
}
