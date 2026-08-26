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

    /** furnitures 테이블의 PK. 도메인 경계를 넘는 JPA 연관 대신 DB FK로만 연결한다. */
    @Column(name = "furniture_id")
    private Long furnitureId;

    private LocalDateTime lastSuccessAt;

    private CoupangKeywordJpaEntity(String keyword, Long furnitureId) {
        this.keyword = keyword;
        this.furnitureId = furnitureId;
    }

    public static CoupangKeywordJpaEntity of(String keyword, Long furnitureId) {
        return new CoupangKeywordJpaEntity(keyword, furnitureId);
    }

    public void markSucceeded(LocalDateTime now) {
        this.lastSuccessAt = now;
    }

}
