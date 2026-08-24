package or.sopt.houme.domain.coupang.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupang_api_call_controls")
@Comment("쿠팡 API 전역 호출 간격 제어")
public class CoupangApiCallControlJpaEntity {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id;

    private LocalDateTime lastCalledAt;

    private CoupangApiCallControlJpaEntity(long id) {
        this.id = id;
    }

    public static CoupangApiCallControlJpaEntity initial() {
        return new CoupangApiCallControlJpaEntity(SINGLETON_ID);
    }

    public boolean canAcquire(LocalDateTime now, long minimumIntervalMinutes) {
        return lastCalledAt == null || !lastCalledAt.plusMinutes(minimumIntervalMinutes).isAfter(now);
    }

    public void acquire(LocalDateTime now) {
        this.lastCalledAt = now;
    }
}
