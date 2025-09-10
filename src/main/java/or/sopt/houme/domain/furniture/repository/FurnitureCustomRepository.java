package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.entity.Furniture;

import java.util.List;

public interface FurnitureCustomRepository {
    List<Furniture> findAllWithTags();
}