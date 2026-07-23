package or.sopt.houme.tag.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 태그(취향) 순수 도메인 모델. JPA 어노테이션이 전혀 없으며, 영속화는 infra 어댑터가 담당한다.
 *
 * <p>기존 {@code tags} 테이블(id, tag_name, priority, tag_name_kr, tag_prompt)의 도메인 표현이다.
 * 우선순위/한글명은 DB에서 unique 제약을 계속 강제하며, 중복 검증은 애플리케이션 계층이 담당한다.
 */
@Getter
public class Tag {

    private final Long id;
    private String tagName;
    private int priority;
    private String tagNameKr;
    private String tagPrompt;

    @Builder
    private Tag(Long id, String tagName, int priority, String tagNameKr, String tagPrompt) {
        this.id = id;
        this.tagName = tagName;
        this.priority = priority;
        this.tagNameKr = tagNameKr;
        this.tagPrompt = tagPrompt;
    }

    /** 신규 생성 (아직 영속화 전이므로 id 없음). */
    public static Tag of(String tagName, int priority, String tagNameKr, String tagPrompt) {
        return new Tag(null, tagName, priority, tagNameKr, tagPrompt);
    }

    /** 영속 데이터로부터 재구성 (infra 매퍼 전용). */
    public static Tag reconstitute(Long id, String tagName, int priority, String tagNameKr, String tagPrompt) {
        return new Tag(id, tagName, priority, tagNameKr, tagPrompt);
    }

    /**
     * 태그 정보를 선택적으로 수정한다. null/blank 인 값은 기존 값을 유지한다.
     * (어드민 태그 수정에서 사용. 기존 Tag 엔티티의 update 로직을 도메인으로 이관)
     */
    public void update(String newTagNameEng, Integer newPriority, String newTagPrompt, String newTagNameKr) {
        if (newTagNameEng != null && !newTagNameEng.isBlank()) {
            this.tagName = newTagNameEng;
        }
        if (newPriority != null) {
            this.priority = newPriority;
        }
        if (newTagPrompt != null && !newTagPrompt.isBlank()) {
            this.tagPrompt = newTagPrompt;
        }
        if (newTagNameKr != null && !newTagNameKr.isBlank()) {
            this.tagNameKr = newTagNameKr;
        }
    }
}
