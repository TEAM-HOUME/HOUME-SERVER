package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.furniture.domain.FurnitureTagView;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureProductsInfoResponseV2;

import java.util.List;

public interface CurationFurnitureService {

    List<FurnitureProductsInfoResponse.FurnitureProductInfo> getCurationProducts(FurnitureTagView furnitureTag, CurationSource source);

    List<FurnitureProductsInfoResponse.FurnitureProductInfo> saveCurationResults(
            FurnitureTagView furnitureTag,
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos,
            CurationSource source
    );

    FurnitureProductsInfoResponseV2 buildProductsInfoResponse(
            Long userId,
            String userName,
            FurnitureTagView furnitureTag,
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> rawInfos
    );
}
