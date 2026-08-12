package or.sopt.houme.carouseltag.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캐러셀-태그 매핑 영속 엔티티. 애플리케이션 로직에서 소비되지 않는 순수 매핑 테이블이며,
 * Carousel·Tag 를 @ManyToOne 대신 carousel_id/tag_id 컬럼(Long)으로만 참조한다(도메인 경계 분리).
 *
 * <p>기존 {@code carousel_tags} 테이블 스키마(id, carousel_id, tag_id)와 매핑이 완전히 동일하다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carousel_tags")
public class CarouselTagJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carousel_id")
    private Long carouselId;

    @Column(name = "tag_id")
    private Long tagId;

    @Builder
    private CarouselTagJpaEntity(Long id, Long carouselId, Long tagId) {
        this.id = id;
        this.carouselId = carouselId;
        this.tagId = tagId;
    }
}
