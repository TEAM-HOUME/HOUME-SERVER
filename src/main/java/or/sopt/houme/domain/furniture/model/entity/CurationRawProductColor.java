package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(
        name = "curation_raw_product_colors",
        indexes = {
                @Index(name = "idx_raw_product_color_product_id", columnList = "curation_raw_product_id"),
                @Index(name = "idx_raw_product_color_client", columnList = "client_color_name")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_raw_product_color_product_id_raw_color",
                        columnNames = {"curation_raw_product_id", "raw_color_name"}
                )
        }
)
@Comment("큐레이션 원본 상품의 색상 정보를 저장하는 엔티티입니다")
public class CurationRawProductColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("식별자")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curation_raw_product_id", nullable = false)
    @Comment("연결된 원본 상품")
    private CurationRawProduct curationRawProduct;

    @Column(name = "raw_color_name")
    @Comment("원본 색상명")
    private String rawColorName;

    @Column(name = "client_color_name")
    @Comment("클라이언트 반환용 색상명")
    private String clientColorName;
}
