package or.sopt.houme.domain.furniture.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(
        name = "curation_furnitures",
        indexes = {
                @Index(name = "idx_curation_furniture_tag_id", columnList = "furniture_tag_id"),
                @Index(name = "idx_curation_recommend_furniture_id", columnList = "recommend_furniture_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_curation_furniture_tag_rank",
                        columnNames = {"furniture_tag_id", "rank"}
                )
        }
)
public class CurationFurniture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_tag_id", nullable = false)
    private FurnitureTag furnitureTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommend_furniture_id", nullable = false)
    private RecommendFurniture recommendFurniture;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "similarity", nullable = false)
    private Double similarity;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public static CurationFurniture of(
            FurnitureTag furnitureTag,
            RecommendFurniture recommendFurniture,
            int rank,
            double similarity,
            LocalDateTime fetchedAt
    ) {
        return CurationFurniture.builder()
                .furnitureTag(furnitureTag)
                .recommendFurniture(recommendFurniture)
                .rank(rank)
                .similarity(similarity)
                .fetchedAt(fetchedAt)
                .build();
    }
}
