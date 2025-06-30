package or.sopt.houme.domain.house.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.generatedImage.entity.GenerateImage;
import or.sopt.houme.domain.user.entity.User;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Form form;

    @Enumerated(EnumType.STRING)
    private Structure structure;

    private Long equilibrium;

    @Enumerated(EnumType.STRING)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToOne(mappedBy = "house")
    private GenerateImage generateImage;
}
