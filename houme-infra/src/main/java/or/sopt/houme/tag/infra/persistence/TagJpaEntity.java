package or.sopt.houme.tag.infra.persistence;

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
 * 태그 영속 엔티티. 도메인 모델({@link or.sopt.houme.tag.domain.Tag})과 분리된 infra 전용 타입.
 *
 * <p>기존 {@code tags} 테이블 스키마(id, tag_name, priority, tag_name_kr, tag_prompt)와 매핑이 완전히 동일하다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tags", indexes = {
        @Index(name = "idx_tag_name_kr", columnList = "tag_name_kr")
})
public class TagJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    @Column(name = "priority", nullable = false, unique = true)
    private int priority;

    @Column(name = "tag_name_kr", nullable = false, unique = true)
    private String tagNameKr;

    @Column(name = "tag_prompt", nullable = false, columnDefinition = "TEXT")
    private String tagPrompt;

    @Builder
    private TagJpaEntity(Long id, String tagName, int priority, String tagNameKr, String tagPrompt) {
        this.id = id;
        this.tagName = tagName;
        this.priority = priority;
        this.tagNameKr = tagNameKr;
        this.tagPrompt = tagPrompt;
    }

    /** 신규 저장용 (id 없음 → INSERT). */
    public static TagJpaEntity forInsert(String tagName, int priority, String tagNameKr, String tagPrompt) {
        return new TagJpaEntity(null, tagName, priority, tagNameKr, tagPrompt);
    }

    /** 도메인 상태를 반영해 필드를 갱신한다(트랜잭션 내 더티 체킹으로 UPDATE 반영). */
    public void apply(String tagName, int priority, String tagNameKr, String tagPrompt) {
        this.tagName = tagName;
        this.priority = priority;
        this.tagNameKr = tagNameKr;
        this.tagPrompt = tagPrompt;
    }
}
