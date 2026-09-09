package or.sopt.houme.compare.infra.persistence;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@Table(name = "compare_presets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComparePresetJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String sourceUrl;

    @Column(nullable = false)
    private String title;

    @Column
    private String thumbnailUrl;

    @Column
    private String brand;

    @Column
    private Long price;

    @Column(nullable = false, length = 10)
    private String currency;

    @Builder
    private ComparePresetJpaEntity(String sourceUrl, String title, String thumbnailUrl,
                                   String brand, Long price, String currency) {
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.brand = brand;
        this.price = price;
        this.currency = currency;
    }

    public void update(String title, String thumbnailUrl, String brand, Long price, String currency) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.brand = brand;
        this.price = price;
        this.currency = currency;
    }
}
