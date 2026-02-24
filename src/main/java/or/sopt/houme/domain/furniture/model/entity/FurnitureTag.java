package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.AdminFurniturePromptRequestDTO;
import or.sopt.houme.domain.house.model.taste.entity.Tag;

@Entity
@Getter
@Table(name = "furniture_tags")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(name = "furniture_url",columnDefinition = "varchar(2048)", nullable = false)
    private String furnitureUrl;

    @Column(name = "search_keyword", nullable = false)
    private String searchKeyword;

    @Column(name = "priority", nullable = false)
    private Integer priority;



    public static FurnitureTag createByAdminFurniturePromptRequestDTO(AdminFurniturePromptRequestDTO dto,
                                                                      Furniture furniture,
                                                                      Tag tag,
                                                                      String furnitureUrl,
                                                                      String searchKeyword,
                                                                      Integer priority){
        return FurnitureTag.builder()
                .furniturePrompt(dto.prompt())
                .furniture(furniture)
                .tag(tag)
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
