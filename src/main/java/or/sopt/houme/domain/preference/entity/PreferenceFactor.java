package or.sopt.houme.domain.preference.entity;

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

    // Factor 업데이트
    public void updateFactor(Factor factor) {
        this.factor = factor;
    }
}
