package or.sopt.houme.domain.taste.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.admin.controller.dto.AdminTagUpdateRequestDTO;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    // 우선순위
    @Column(name = "priority", nullable = false)
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

    public void update(AdminTagUpdateRequestDTO dto) {

        if (dto.newTagNameEng() != null) {
            this.tagName = dto.newTagNameEng();
        }
        if (dto.newPriority() != null) {
            this.priority = dto.newPriority();
        }
        if (dto.newTagPrompt() != null) {
            this.tagPrompt = dto.newTagPrompt();
        }
    }

}
