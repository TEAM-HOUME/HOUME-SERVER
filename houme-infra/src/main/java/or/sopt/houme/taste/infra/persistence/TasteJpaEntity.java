package or.sopt.houme.taste.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 취향(무드보드) 영속 엔티티. 도메인 모델({@link or.sopt.houme.taste.domain.Taste})과 분리된 infra 전용 타입.
 *
 * <p>기존 {@code tastes} 테이블 스키마(id, url, filename, original_filename, file_extension)와 매핑이 완전히 동일하다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tastes", indexes = {
        @Index(name = "idx_filename", columnList = "filename", unique = true),
})
public class TasteJpaEntity {

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

    @Builder
    private TasteJpaEntity(Long id, String url, String filename, String originalFilename, String fileExtension) {
        this.id = id;
        this.url = url;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileExtension = fileExtension;
    }

    /** presigned URL 발급 결과로부터 신규 무드보드 엔티티 생성 (id 없음 → INSERT). */
    public static TasteJpaEntity createByPreSignedURL(String url, String filename, String originalFilename, String fileExtension) {
        return TasteJpaEntity.builder()
                .url(url)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(fileExtension)
                .build();
    }
}
