package or.sopt.houme.domain.generateImage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "image_generation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageGenerationLog extends BaseEntity {
    // 이미지 생성 (A/B 추적 테이블)

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 생성 유저
    private Long userId;

    // A/B 타입
    @Comment(value = "A/B 타입")
    @Column(nullable = false, length = 10)
    private String type;

    // 선택한 무드보드 개수
    @Comment(value = "선택한 무드보드 개수")
    private Integer selectedMoodboardCount;

    // 생성된 이미지 개수
    @Comment(value = "생성된 이미지 개수")
    private Integer generatedImageCount;

    // 선택한 무드보드 식별자들 (Json 타입으로 저장)
    @Comment(value = "선택한 무드보드 식별자들 (Json 타입)") // 컬럼에 주석 추가하는 어노테이션
    @JdbcTypeCode(SqlTypes.JSON)    // JSON 문자열 <-> PostgreSQL의 JSONB 타입 변환 처리
    @Column(columnDefinition = "jsonb") // 컬럼 타입을 JSONB로 명시 지정
    private String selectedMoodboardIds;

    // 선택한 무드보드 이름들 (Json 타입으로 저장)
    @Comment(value = "선택한 무드보드 이름들 (Json 타입)")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String selectedMoodboardNames;

    // 선택한 스타일 태그 식별자들 (Json 타입으로 저장)
    @Comment(value = "선택한 스타일 태그 식별자들 (Json 타입)")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String selectedStyleTagIds;

    // 선택한 스타일 태그 이름들 (Json 타입으로 저장)
    @Comment(value = "선택한 스타일 태그 이름들 (Json 타입)")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String selectedStyleTagNames;

    @OneToMany(mappedBy = "imageGenerationLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageGenerationDetail> imageGenerationDetailList = new ArrayList<>();

    /**
     * 생성자
     */
    @Builder
    public ImageGenerationLog(Long userId, String type, Integer selectedMoodboardCount,
                              Integer generatedImageCount, String selectedMoodboardIds, String selectedMoodboardNames,
                              String selectedStyleTagIds, String selectedStyleTagNames) {
        this.userId = userId;
        this.type = type;
        this.selectedMoodboardCount = selectedMoodboardCount;
        this.generatedImageCount = generatedImageCount;
        this.selectedMoodboardIds = selectedMoodboardIds;
        this.selectedMoodboardNames = selectedMoodboardNames;
        this.selectedStyleTagIds = selectedStyleTagIds;
        this.selectedStyleTagNames = selectedStyleTagNames;
    }


    /**
     * 도메인 로직
     */
    public void addGeneratedImage(ImageGenerationDetail image) {
        imageGenerationDetailList.add(image);
        image.setImageGenerationLog(this);
    }
}
