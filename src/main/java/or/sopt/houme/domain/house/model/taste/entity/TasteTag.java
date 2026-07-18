package or.sopt.houme.domain.house.model.taste.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.tag.infra.persistence.TagJpaEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "taste_tags")
public class TasteTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taste_id")
    private Taste taste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private TagJpaEntity tag;


    public static TasteTag of(Taste taste, TagJpaEntity tag) {
        return TasteTag.builder()
                .taste(taste)
                .tag(tag)
                .build();
    }
}
