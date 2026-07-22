package or.sopt.houme.house.infra.persistence;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "houses")
public class HouseJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity", nullable = true)
    private Activity activity;

    // #582: User 연관 절단 — @ManyToOne 대신 user_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banner_id")
    private Banner banner;

    @OneToMany(mappedBy = "house")
    @Builder.Default
    private List<GenerateImage> generateImages = new ArrayList<>();

    @OneToMany(mappedBy = "house")
    private List<HouseFloorPlan> houseFloorPlans = new ArrayList<>();


    // 입력값이 유효한지에 대한 여부 (true = 유효한 값)
    @Column(name = "is_valid", nullable = false)
    private boolean isValid = true;

    // 완성된 프롬프트를 저장합니다
    @Column(name = "house_prompt", columnDefinition = "TEXT", nullable = true)
    private String housePrompt;

    // Activity 업데이트 하기
    public void updateActivity(Activity activity) {
        this.activity = activity;
    }

    // 프롬프트 저장하기
    public void updatePrompt(String prompt) {
        this.housePrompt = prompt;
    }
}
