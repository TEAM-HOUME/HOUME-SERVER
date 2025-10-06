package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.*;
import or.sopt.houme.domain.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommend_furniture_id", nullable = false)
    private RecommendFurniture recommendFurniture;

    public static Jjym of(User user, RecommendFurniture recommendFurniture) {
        return Jjym.builder()
                .user(user)
                .recommendFurniture(recommendFurniture)
                .build();
    }

    @Version
    private Long version;
}
