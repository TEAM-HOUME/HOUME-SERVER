package or.sopt.houme.domain.preference.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.Entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Preference extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_like", nullable = false)
    private boolean isLike;
}
