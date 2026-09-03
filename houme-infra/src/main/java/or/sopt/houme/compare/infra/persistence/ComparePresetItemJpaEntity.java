package or.sopt.houme.compare.infra.persistence;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.entity.BaseEntity;

import java.time.OffsetDateTime;

@Entity
@Table(name = "compare_preset_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComparePresetItemJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_id", nullable = false)
    private ComparePresetJpaEntity preset;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String title;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column
    private String siteName;

    @Column(nullable = false, length = 1000)
    private String productUrl;

    @Column(nullable = false)
    private OffsetDateTime priceUpdatedAt;

    @Builder
    private ComparePresetItemJpaEntity(ComparePresetJpaEntity preset, String source, String productId,
                                       String title, String imageUrl, Double price, String currency,
                                       String siteName, String productUrl, OffsetDateTime priceUpdatedAt) {
        this.preset = preset;
        this.source = source;
        this.productId = productId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.price = price;
        this.currency = currency;
        this.siteName = siteName;
        this.productUrl = productUrl;
        this.priceUpdatedAt = priceUpdatedAt;
    }
}
