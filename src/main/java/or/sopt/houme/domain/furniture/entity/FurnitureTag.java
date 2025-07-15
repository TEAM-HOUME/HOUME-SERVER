package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.taste.entity.Tag;

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
}
