package or.sopt.houme.domain.preference.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "preferences")
public class Preference extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_like", nullable = false)
    private boolean isLike;

    public static Preference of(boolean like){
        return Preference.builder()
                .isLike(like)
                .build();
    }

    public void updateLike(boolean isLike) {
        this.isLike = isLike;
    }
}
