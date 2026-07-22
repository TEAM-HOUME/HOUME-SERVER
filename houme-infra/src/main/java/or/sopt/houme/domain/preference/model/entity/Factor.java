package or.sopt.houme.domain.preference.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
@Table(name = "factors")
public class Factor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factor_text", nullable = false)
    private String factorText;

    @Column(name = "is_like", nullable = false)
    private boolean isLike;
}
