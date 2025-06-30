package or.sopt.houme.domain.s3test.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.S3Util;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3TestService {

    private final S3Util s3Util;

    public String uploadImage(MultipartFile file) {

        String dirName = "test";

        ImageUploadResponseDTO upload = s3Util.upload(dirName, file);

        return "imageLink:   "+upload.getImageLink() + " <br> filename:   " + upload.getFilename();
    }

    public void deleteImage(String filename) {

        s3Util.delete(filename);
    }
}
