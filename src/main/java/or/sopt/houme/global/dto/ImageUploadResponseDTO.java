package or.sopt.houme.global.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponseDTO {

    private String filename;
    private String originalFilename;
    private String imageLink;
    private String contentType;

    @Setter
    private Float clipScore;

    @Setter
    private String pullPrompt;

    public static ImageUploadResponseDTO from(String filename, String originalFilename, String imagePath, String contentType) {
        return ImageUploadResponseDTO.builder()
                .filename(filename)
                .originalFilename(originalFilename)
                .imageLink(imagePath)
                .contentType(contentType)
                .build();
    }
}
