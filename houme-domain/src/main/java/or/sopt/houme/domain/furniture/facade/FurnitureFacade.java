package or.sopt.houme.domain.furniture.facade;

import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.forPlan.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.user.entity.User;

public interface FurnitureFacade {
    FurnitureProductsInfoResponse getFurnitureProductInfoFromNaverApi(User user, Long imageId, Long categoryId);

    FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlan(User user, Long tagId, Long furnitureId, String searchKeyword, int pHash);

    FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlanV2(
            User user,
            Long tagId,
            Long furnitureId,
            String searchKeyword,
            int pHash,
            java.util.List<String> allowedMalls,
            Boolean applyNaverPay
    );
}
