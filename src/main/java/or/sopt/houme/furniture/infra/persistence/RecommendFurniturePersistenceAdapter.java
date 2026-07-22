package or.sopt.houme.furniture.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.furniture.domain.RecommendFurniture;
import or.sopt.houme.furniture.domain.port.out.RecommendFurniturePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link RecommendFurniturePort} 의 JPA 구현 어댑터.
 */
@Component
@RequiredArgsConstructor
public class RecommendFurniturePersistenceAdapter implements RecommendFurniturePort {

    private final RecommendFurnitureRepository recommendFurnitureRepository;

    private static RecommendFurniture toDomain(or.sopt.houme.domain.furniture.model.entity.RecommendFurniture entity) {
        return RecommendFurniture.reconstitute(
                entity.getId(),
                entity.getFurnitureProductImageUrl(),
                entity.getFurnitureProductSiteUrl(),
                entity.getFurnitureProductName(),
                entity.getFurnitureProductMallName(),
                entity.getFurnitureProductId(),
                entity.getSource()
        );
    }

    @Override
    public Optional<RecommendFurniture> findById(Long id) {
        return recommendFurnitureRepository.findById(id).map(RecommendFurniturePersistenceAdapter::toDomain);
    }

    @Override
    public List<RecommendFurniture> findAllByIdIn(List<Long> ids) {
        return recommendFurnitureRepository.findAllById(ids).stream()
                .map(RecommendFurniturePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<RecommendFurniture> findBySourceAndFurnitureProductId(CurationSource source, Long furnitureProductId) {
        return recommendFurnitureRepository.findBySourceAndFurnitureProductId(source, furnitureProductId)
                .map(RecommendFurniturePersistenceAdapter::toDomain);
    }

    @Override
    public List<RecommendFurniture> findAllBySourceAndFurnitureProductIdIn(CurationSource source, List<Long> furnitureProductIds) {
        return recommendFurnitureRepository.findAllBySourceAndFurnitureProductIdIn(source, furnitureProductIds).stream()
                .map(RecommendFurniturePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public RecommendFurniture save(RecommendFurniture recommendFurniture) {
        return toDomain(recommendFurnitureRepository.save(
                or.sopt.houme.domain.furniture.model.entity.RecommendFurniture.from(
                        recommendFurniture.getFurnitureProductImageUrl(),
                        recommendFurniture.getFurnitureProductSiteUrl(),
                        recommendFurniture.getFurnitureProductName(),
                        recommendFurniture.getFurnitureProductMallName(),
                        recommendFurniture.getFurnitureProductId(),
                        recommendFurniture.getSource()
                )
        ));
    }
}
