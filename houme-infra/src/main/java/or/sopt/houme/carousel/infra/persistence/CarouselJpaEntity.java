package or.sopt.houme.carousel.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.house.model.carousel.entity.CarouselType;

/**
 * 캐러셀 영속 엔티티. 도메인 모델({@link or.sopt.houme.carousel.domain.Carousel})과 분리된 infra 전용 타입.
 *
 * <p>기존 {@code carousels} 테이블 스키마와 매핑이 완전히 동일하다. CarouselType 은 클러스터 밖 조회용 엔티티라
 * infra 계층에서 @ManyToOne 로 유지한다(도메인 모델은 carouselTypeId 만 안다).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carousels")
public class CarouselJpaEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carousel_type_id")
    private CarouselType carouselType;

    @Builder
    private CarouselJpaEntity(Long id, String url, String filename, String originalFilename,
                             String fileExtension, CarouselType carouselType) {
        this.id = id;
        this.url = url;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileExtension = fileExtension;
        this.carouselType = carouselType;
    }
}
