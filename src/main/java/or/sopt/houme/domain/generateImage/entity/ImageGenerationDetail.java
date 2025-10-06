package or.sopt.houme.domain.generateImage.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "image_generation_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageGenerationDetail extends BaseEntity {
    /**
     *  A/B 이미지 생성 상세
     */

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment(value = "생성된 이미지 주소")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String generatedImageUrl;

    @Comment(value = "생성된 이미지 식별자")
    private Long generatedImageId;

    @Comment(value = "이미지 생성때 사용된 스타일 태그 식별자")
    private Long selectedStyleTagId;

    @Comment(value = "이미지 생성때 사용된 스타일 태그 이름")
    private String selectedStyleTagName;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private ImageGenerationLog imageGenerationLog;  // 생성 이미지 요청 로그

    /**
     * 생성자
     */
    @Builder
    public ImageGenerationDetail(String imageUrl, Long imageId, Long styleTagId,
                                 String styleTagName, ImageGenerationLog imageGenerationLog) {
        this.generatedImageUrl = imageUrl;
        this.generatedImageId = imageId;
        this.selectedStyleTagId = styleTagId;
        this.selectedStyleTagName = styleTagName;
        this.imageGenerationLog = imageGenerationLog;
    }
}
