package or.sopt.houme.domain.preference.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "preferences_factors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"preference_id", "factor_id"}))
public class PreferenceFactor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id")
    private Preference preference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factor_id")
    private Factor factor;

    // 정적 팩터리 메서드
    public static PreferenceFactor of(Preference preference, Factor factor) {
        return PreferenceFactor.builder()
                .preference(preference)
                .factor(factor)
                .build();
    }

    /**
     * PreferenceFactor의 factor를 업데이트합니다.
     * 주로 중복 factor 제거 시 기존 엔티티를 재사용할 때 사용됩니다.
     * @param factor 새로운 Factor (null 불가)
     */
    public void updateFactor(Factor factor) {
        this.factor = factor;
    }
}
