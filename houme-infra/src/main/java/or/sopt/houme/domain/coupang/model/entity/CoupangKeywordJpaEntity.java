package or.sopt.houme.domain.coupang.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupang_keywords")
@Comment("쿠팡 파트너스 배치 검색 키워드")
public class CoupangKeywordJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String keyword;

    @Column(nullable = false, length = 50)
    private String category;

    private LocalDateTime lastSuccessAt;

    private CoupangKeywordJpaEntity(String keyword, String category) {
        this.keyword = keyword;
        this.category = category;
    }

    public static CoupangKeywordJpaEntity of(String keyword, String category) {
        return new CoupangKeywordJpaEntity(keyword, category);
    }

    public void markSucceeded(LocalDateTime now) {
        this.lastSuccessAt = now;
    }

}
