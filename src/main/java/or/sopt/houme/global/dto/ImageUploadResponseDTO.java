package or.sopt.houme.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponseDTO {

    private String filename;
    private String originalFilename;
    private String imagePath;

    public static ImageUploadResponseDTO from(String filename, String originalFilename, String imagePath) {
        return ImageUploadResponseDTO.builder()
                .filename(filename)
                .originalFilename(originalFilename)
                .imagePath(imagePath)
                .build();
    }
}
