package or.sopt.houme.domain.generatedImage.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.entity.House;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class GenerateImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Enumerated(EnumType.STRING)
    private Type type;

    @OneToOne
    @JoinColumn(name = "house_id")
    private House house;
}
