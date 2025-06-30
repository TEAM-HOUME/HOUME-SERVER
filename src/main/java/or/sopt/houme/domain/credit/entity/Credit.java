package or.sopt.houme.domain.credit.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.entity.User;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Credit{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
