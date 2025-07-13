package or.sopt.houme.domain.preference.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.entity.House;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "prompt_preferences")
public class PromptPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id")
    private Preference preference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private House house;

    // 정적 메서드
    public static PromptPreference generatePreference(Preference preference, House house) {
        return PromptPreference.builder()
                .preference(preference)
                .house(house)
                .build();
    }
}
