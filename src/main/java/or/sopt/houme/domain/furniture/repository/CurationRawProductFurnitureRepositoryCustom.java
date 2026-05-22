package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;

import java.util.List;

public interface CurationRawProductFurnitureRepositoryCustom {

    List<CurationRawProductFurniture> findAllByCurationRawProductIdInWithFurniture(List<Long> rawProductIds);
}
