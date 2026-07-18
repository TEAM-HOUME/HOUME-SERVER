package or.sopt.houme.domain.house.model.taste.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "tags", indexes = {
        @Index(name = "idx_tag_name_kr", columnList = "tag_name_kr")
})
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    // 우선순위
    @Column(name = "priority", nullable = false, unique = true)
    private int priority;

    // 태그(취향) 이름 한글
    @Column(name = "tag_name_kr", nullable = false, unique = true)
    private String tagNameKr;

    // 태그(취향) 프롬프트
    @Column(name = "tag_prompt", nullable = false, columnDefinition = "TEXT")
    private  String tagPrompt;



    public static Tag of(String tagName, int priority, String tagNameKr, String tagPrompt) {
        return Tag.builder()
                .tagName(tagName)
                .priority(priority)
                .tagNameKr(tagNameKr)
                .tagPrompt(tagPrompt)
                .build();
    }

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
