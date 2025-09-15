package or.sopt.houme.domain.preference.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "preferences_factors",
        uniqueConstraints = @UniqueConstraint(columnNames = "preference_id"))
public class PreferencesFactor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id")
    private Preference preference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factor_id")
    private Factor factor;
}
