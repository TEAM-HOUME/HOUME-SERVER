package or.sopt.houme.domain.preference.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "generate_image_preference", uniqueConstraints = {
        // 하나의 이미지에 하나의 선호도만 존재하도록 설정
        @UniqueConstraint(name = "uk_prompt_pref_generate_image", columnNames = "generate_image_id")
})
public class GenerateImagePreference {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id", unique = true)
    private Preference preference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generate_image_id")
    private GenerateImage generateImage;

    // 정적 메서드
    public static GenerateImagePreference generatePreference(Preference preference, GenerateImage generateImage) {
        return GenerateImagePreference.builder()
                .preference(preference)
                .generateImage(generateImage)
                .build();
    }

    @Builder
    public GenerateImagePreference(Preference preference, GenerateImage generateImage) {
        this.preference = preference;
        this.generateImage = generateImage;
    }
}

