package or.sopt.houme.furniture.domain.port.out;

import or.sopt.houme.furniture.domain.Jjym;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 찜 영속화 아웃바운드 포트. 도메인/애플리케이션은 이 인터페이스만 알고,
 * 구현(JPA·QueryDSL)은 infra 어댑터가 제공한다.
 */
public interface JjymRepositoryPort {

    Optional<Jjym> findByUserIdAndRecommendFurnitureId(Long userId, Long recommendFurnitureId);

    Jjym save(Jjym jjym);

    void deleteById(Long jjymId);

    void deleteByUserId(Long userId);

    boolean existsByUserIdAndRecommendFurnitureId(Long userId, Long recommendFurnitureId);

    /** 유저의 찜 목록 (created_at DESC 정렬). */
    List<Jjym> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Jjym> findAllByUserIdAndRecommendFurnitureIdIn(Long userId, List<Long> recommendFurnitureIds);

    long countByRecommendFurnitureId(Long recommendFurnitureId);

    Map<Long, Long> countByRecommendFurnitureIds(List<Long> recommendFurnitureIds);
}
