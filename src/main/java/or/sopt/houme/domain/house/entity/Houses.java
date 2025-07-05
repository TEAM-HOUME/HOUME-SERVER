package or.sopt.houme.domain.house.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.generatedImage.entity.GenerateImages;
import or.sopt.houme.domain.user.entity.Users;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Houses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "form", nullable = false)
    private Form form;

    @Enumerated(EnumType.STRING)
    @Column(name = "structure", nullable = false)
    private Structure structure;

    @Enumerated(EnumType.STRING)
    @Column(name = "equilibrium", nullable = false)
    private Equilibrium equilibrium;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity", nullable = true)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToOne(mappedBy = "house")
    private GenerateImages generateImage;

    // 입력값이 유효한지에 대한 여부
    @Column(name = "is_valid", nullable = false)
    private boolean isValid;
}
