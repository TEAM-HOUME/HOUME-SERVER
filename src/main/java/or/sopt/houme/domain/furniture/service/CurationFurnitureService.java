package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;

import java.util.List;

public interface CurationFurnitureService {

    List<FurnitureProductsInfoResponse.FurnitureProductInfo> getCurationProducts(FurnitureTag furnitureTag);

    List<FurnitureProductsInfoResponse.FurnitureProductInfo> saveCurationResults(
            FurnitureTag furnitureTag,
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos
    );
}
