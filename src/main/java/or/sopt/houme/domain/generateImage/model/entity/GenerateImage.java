package or.sopt.houme.domain.generateImage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.house.model.entity.House;
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

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private House house;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_type", length = 20)
    private GenerateImageType generationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banner_id")
    private Banner banner;

    // 정적 메서드
    public static GenerateImage createGenerateImage(ImageUploadResponseDTO request, House house) {
        return createGenerateImage(request, house, GenerateImageType.RECOMMEND, null);
    }

    public static GenerateImage createGenerateImage(
            ImageUploadResponseDTO request,
            House house,
            GenerateImageType generationType,
            Banner banner
    ) {
        return GenerateImage.builder()
                .url(request.getImageLink())
                .filename(request.getFilename())
                .originalFilename(request.getOriginalFilename())
                .fileExtension(request.getContentType())
                .house(house)
                .generationType(generationType)
                .banner(banner)
                .build();
    }

    public GenerateImageType getResolvedGenerationType() {
        if (generationType != null) {
            return generationType;
        }
        return banner != null ? GenerateImageType.LIST : GenerateImageType.RECOMMEND;
    }
}
