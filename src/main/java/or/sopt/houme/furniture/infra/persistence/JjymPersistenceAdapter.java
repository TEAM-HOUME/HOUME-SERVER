package or.sopt.houme.furniture.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.furniture.domain.Jjym;
import or.sopt.houme.furniture.domain.port.out.JjymRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link JjymRepositoryPort} 의 JPA/QueryDSL 구현 어댑터.
 * 기존 {@link JjymRepository} 를 재사용하고, 경계를 넘을 때만 순수 도메인으로 매핑한다.
 */
@Component
@RequiredArgsConstructor
public class JjymPersistenceAdapter implements JjymRepositoryPort {

    private final JjymRepository jjymRepository;

    private static Jjym toDomain(or.sopt.houme.domain.furniture.model.entity.Jjym entity) {
        return Jjym.reconstitute(entity.getId(), entity.getUserId(), entity.getRecommendFurnitureId());
    }

    @Override
    public Optional<Jjym> findByUserIdAndRecommendFurnitureId(Long userId, Long recommendFurnitureId) {
        return jjymRepository.findByUserIdAndRecommendFurnitureId(userId, recommendFurnitureId)
                .map(JjymPersistenceAdapter::toDomain);
    }

    @Override
    public Jjym save(Jjym jjym) {
        return toDomain(jjymRepository.save(
                or.sopt.houme.domain.furniture.model.entity.Jjym.of(jjym.getUserId(), jjym.getRecommendFurnitureId())
        ));
    }

    @Override
    public void deleteById(Long jjymId) {
        jjymRepository.deleteById(jjymId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jjymRepository.deleteByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndRecommendFurnitureId(Long userId, Long recommendFurnitureId) {
        return jjymRepository.existsByUserIdAndRecommendFurnitureId(userId, recommendFurnitureId);
    }

    @Override
    public List<Jjym> findAllByUserIdOrderByCreatedAtDesc(Long userId) {
        return jjymRepository.findAllByUserIdWithFurnitureOrderByCreatedAtDesc(userId).stream()
                .map(JjymPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Jjym> findAllByUserIdAndRecommendFurnitureIdIn(Long userId, List<Long> recommendFurnitureIds) {
        return jjymRepository.findAllByUserIdAndRecommendFurnitureIdIn(userId, recommendFurnitureIds).stream()
                .map(JjymPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public long countByRecommendFurnitureId(Long recommendFurnitureId) {
        return jjymRepository.countByRecommendFurnitureId(recommendFurnitureId);
    }

    @Override
    public Map<Long, Long> countByRecommendFurnitureIds(List<Long> recommendFurnitureIds) {
        return jjymRepository.countByRecommendFurnitureIds(recommendFurnitureIds);
    }
}
