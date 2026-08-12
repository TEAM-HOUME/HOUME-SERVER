package or.sopt.houme.tastetag.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 무드보드(취향)-태그 매핑 순수 도메인 모델. JPA 어노테이션이 없으며, 연관은 id 참조로만 표현한다.
 *
 * <p>기존 {@code taste_tags} 테이블(id, taste_id, tag_id)의 도메인 표현이다.
 * (Taste·Tag 를 @ManyToOne 로 물지 않고 tasteId/tagId 로만 참조 — 도메인 경계 분리)
 */
@Getter
public class TasteTag {

    private final Long id;
    private final Long tasteId;
    private final Long tagId;

    @Builder
    private TasteTag(Long id, Long tasteId, Long tagId) {
        this.id = id;
        this.tasteId = tasteId;
        this.tagId = tagId;
    }

    /** 신규 매핑 생성 (아직 영속화 전이므로 id 없음). */
    public static TasteTag of(Long tasteId, Long tagId) {
        return new TasteTag(null, tasteId, tagId);
    }

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static TasteTag reconstitute(Long id, Long tasteId, Long tagId) {
        return new TasteTag(id, tasteId, tagId);
    }
}
