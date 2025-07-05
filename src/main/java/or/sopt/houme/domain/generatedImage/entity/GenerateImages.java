package or.sopt.houme.domain.generatedImage.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.entity.Houses;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class GenerateImages extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @OneToOne
    @JoinColumn(name = "house_id")
    private Houses house;
}
