package or.sopt.houme.domain.house.model.taste.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.moodboard.AdminMoodBoardCreateRequestDTO;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "tastes", indexes = {
        @Index(name = "idx_filename", columnList = "filename", unique = true),
})
public class Taste {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;



    public static Taste createByPreSignedURL(S3PresignedUrlResponseDTO presignedUrl, AdminMoodBoardCreateRequestDTO requestDTO){
        return Taste.builder()
                .url(presignedUrl.publicUrl())
                .filename(presignedUrl.keyName())
                .originalFilename(requestDTO.originalFilename())
                .fileExtension(requestDTO.imageExtension())
                .build();
    }
}
