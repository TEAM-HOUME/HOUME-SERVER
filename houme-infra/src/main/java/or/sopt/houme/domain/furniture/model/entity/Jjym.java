package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.global.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(
        name = "jjyms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_recommend_furniture", columnNames = {"user_id", "recommend_furniture_id"})
        },
        indexes = {
                @Index(name = "idx_jjym_user_id", columnList = "user_id"),
                @Index(name = "idx_jjym_recommend_furniture_id", columnList = "recommend_furniture_id")
        }
)
public class Jjym extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // #582: User 연관 절단 — @ManyToOne 대신 user_id(Long) 컬럼으로만 참조(도메인 경계 분리, FK 는 DB 가 계속 강제)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // #582: RecommendFurniture 연관 절단 — @ManyToOne 대신 recommend_furniture_id(Long) 컬럼으로만 참조(FK 는 DB 가 계속 강제)
    @Column(name = "recommend_furniture_id", nullable = false)
    private Long recommendFurnitureId;

    public static Jjym of(Long userId, Long recommendFurnitureId) {
        return Jjym.builder()
                .userId(userId)
                .recommendFurnitureId(recommendFurnitureId)
                .build();
    }

    @Version
    private Long version;
}
