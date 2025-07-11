package or.sopt.houme.domain.generateImage.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.generateImage.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "generate_images")
public class GenerateImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "original_file_name", nullable = false)
    private String originalFilename;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @OneToOne
    @JoinColumn(name = "house_id")
    private House house;

    // 정적 메서드
    public static GenerateImage createGenerateImage(ImageUploadResponseDTO request, House house) {
        return GenerateImage.builder()
                .url(request.getImageLink())
                .filename(request.getFilename())
                .originalFilename(request.getOriginalFilename())
                .fileExtension(request.getContentType())
                .house(house)
                .build();
    }
}
