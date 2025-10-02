package or.sopt.houme.domain.furniture.facade;

import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.user.entity.User;

public interface FurnitureFacade {
    FurnitureProductsInfoResponse getFurnitureProductInfoFromNaverApi(User user, Long imageId, Long categoryId);
}
