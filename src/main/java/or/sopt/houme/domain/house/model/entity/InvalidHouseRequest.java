package or.sopt.houme.domain.house.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.global.entity.BaseEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class InvalidHouseRequest extends BaseEntity {
    // 유효하지 않은 집들 저장 엔티티

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // #582: User 연관 절단 —  대신 user_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "user_id")
    private Long userId;
}
