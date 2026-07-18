package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(
        name = "furniture_tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_furniture_tags_furniture_id_tag_id",
                columnNames = {"furniture_id", "tag_id"}
        )
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class FurnitureTag {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 가구 스타일별 프롬프트
    @Column(name = "furniture_prompt", columnDefinition = "TEXT", nullable = false)
    private String furniturePrompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_id")
    private Furniture furniture;

    // #582: Tag 연관 절단 — @ManyToOne 대신 tag_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "furniture_url",columnDefinition = "varchar(2048)", nullable = false)
    private String furnitureUrl;

    @Column(name = "search_keyword", nullable = false)
    private String searchKeyword;

    @Column(name = "priority", nullable = false)
    private Integer priority;



    public static FurnitureTag createByAdminFurniturePromptRequestDTO(String prompt,
                                                                      Furniture furniture,
                                                                      Long tagId,
                                                                      String furnitureUrl,
                                                                      String searchKeyword,
                                                                      Integer priority){
        return FurnitureTag.builder()
                .furniturePrompt(prompt)
                .furniture(furniture)
                .tagId(tagId)
                .furnitureUrl(furnitureUrl)
                .searchKeyword(searchKeyword)
                .priority(priority)
                .build();
    }

    public void updatePrompt(String prompt) {
        this.furniturePrompt = prompt;
    }

    public void updateSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public void updatePriority(Integer priority) {
        this.priority = priority;
    }

    public void updateFurnitureUrl(String furnitureUrl) {
        this.furnitureUrl = furnitureUrl;
    }
}
