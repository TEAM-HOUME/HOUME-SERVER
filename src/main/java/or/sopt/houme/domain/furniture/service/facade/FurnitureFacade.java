package or.sopt.houme.domain.furniture.service.facade;

import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.forPlan.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureProductsInfoResponseV2;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;

public interface FurnitureFacade {
    FurnitureProductsInfoResponseV2 getFurnitureProductInfoFromNaverApi(UserJpaEntity user, Long imageId, Long categoryId);

    FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlan(UserJpaEntity user, Long tagId, Long furnitureId, String searchKeyword, int pHash);

    FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlanV2(
            UserJpaEntity user,
            Long tagId,
            Long furnitureId,
            String searchKeyword,
            int pHash,
            java.util.List<String> allowedMalls,
            Boolean applyNaverPay
    );
}
