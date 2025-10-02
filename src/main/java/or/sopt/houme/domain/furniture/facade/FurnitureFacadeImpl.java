package or.sopt.houme.domain.furniture.facade;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.furniture.service.ImageHashService;
import or.sopt.houme.domain.furniture.service.NaverShopService;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FurnitureFacadeImpl implements FurnitureFacade {
    private final NaverShopService naverShopService;
    private final ImageHashService imageHashService;
    private final FurnitureService furnitureService;

    @Override
    public FurnitureProductsInfoResponse getFurnitureProductInfoFromNaverApi(User user, Long imageId, Long categoryId) {
        // 1. FurnitureTag 조회 (DB)
        FurnitureTag furnitureTag = furnitureService.findFurnitureTag(user, imageId, categoryId);

        // 2. 네이버 API 호출
        String keyword = furnitureTag.getSearchKeyword();
        List<NaverFurnitureProductDto> products = naverShopService.search(keyword, 20);

        // 3. FastAPI 호출 → 유사도 기반 상위 상품 리스트만 반환
        List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos =
                imageHashService.rankByImageSimilarity(furnitureTag.getFurnitureUrl(), products, 5);

        // 4. 최종 응답 조립 (Facade 책임)
        return FurnitureProductsInfoResponse.of(user.getName(), infos);
    }
}
