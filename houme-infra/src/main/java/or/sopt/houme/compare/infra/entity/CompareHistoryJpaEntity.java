package or.sopt.houme.compare.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "compare_history")
@Comment("가격비교 이력")
public class CompareHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_url", nullable = false, length = 2048)
    @Comment("비교 요청 원본 URL")
    private String sourceUrl;

    @Column(name = "title", length = 500)
    @Comment("스크래핑된 상품명")
    private String title;

    @Column(name = "thumbnail", length = 2048)
    @Comment("스크래핑된 썸네일 이미지 URL")
    private String thumbnail;

    @Column(name = "price")
    @Comment("스크래핑된 가격 (KRW)")
    private Long price;
}
