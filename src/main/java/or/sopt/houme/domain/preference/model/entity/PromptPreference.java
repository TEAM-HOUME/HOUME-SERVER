package or.sopt.houme.domain.preference.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.model.entity.House;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "prompt_preferences", uniqueConstraints = {
        // 하나의 집에 대해 한 건의 선호도만 존재하도록 설정
        @UniqueConstraint(name = "uk_prompt_pref_house", columnNames = "house_id")
})
public class PromptPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id", unique = true)
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
