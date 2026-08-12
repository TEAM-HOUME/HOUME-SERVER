package or.sopt.houme.tastetag.infra.persistence;

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
 * 무드보드-태그 매핑 영속 엔티티. 도메인 모델({@link or.sopt.houme.tastetag.domain.TasteTag})과 분리된 infra 전용 타입.
 *
 * <p>기존 {@code taste_tags} 테이블 스키마(id, taste_id, tag_id)와 매핑이 완전히 동일하다.
 * Taste·Tag 를 @ManyToOne 대신 taste_id/tag_id 컬럼(Long)으로만 참조해 도메인 순수성을 유지한다(FK 는 DB 가 계속 강제).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "taste_tags")
public class TasteTagJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "taste_id")
    private Long tasteId;

    @Column(name = "tag_id")
    private Long tagId;

    @Builder
    private TasteTagJpaEntity(Long id, Long tasteId, Long tagId) {
        this.id = id;
        this.tasteId = tasteId;
        this.tagId = tagId;
    }

    public static TasteTagJpaEntity forInsert(Long tasteId, Long tagId) {
        return new TasteTagJpaEntity(null, tasteId, tagId);
    }
}
