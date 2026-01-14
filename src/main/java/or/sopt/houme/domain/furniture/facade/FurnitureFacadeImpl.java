package or.sopt.houme.domain.furniture.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.forPlan.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.service.CurationFurnitureService;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.furniture.service.ImageHashService;
import or.sopt.houme.domain.furniture.service.NaverShopService;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import or.sopt.houme.global.config.NaverProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class FurnitureFacadeImpl implements FurnitureFacade {

    private final NaverShopService naverShopService;
    private final ImageHashService imageHashService;
    private final FurnitureService furnitureService;
    private final CurationFurnitureService curationFurnitureService;
    private final NaverProperties naverProperties;

    @Override
    public FurnitureProductsInfoResponse getFurnitureProductInfoFromNaverApi(User user, Long imageId, Long categoryId) {


        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 1. FurnitureTag 조회 (DB)
        log.info("연관된 가구들을 조회합니다:{}",formatted);
        FurnitureTag furnitureTag = furnitureService.findFurnitureTag(user, imageId, categoryId);

        List<FurnitureProductsInfoResponse.FurnitureProductInfo> cachedInfos =
                curationFurnitureService.getCurationProducts(furnitureTag);
        if (!cachedInfos.isEmpty()) {
            log.info("큐레이션 결과를 DB에서 조회합니다");
            return FurnitureProductsInfoResponse.of(user.getName(), cachedInfos);
        }

        // 2. DB에서 조회된 결과가 없는 경우 -> 네이버 API 호출
        log.info("네이버 API 호출을 시작합니다");
        String keyword = furnitureTag.getSearchKeyword();
        List<NaverFurnitureProductDto> products = naverShopService.search(keyword, 50);

        // 3. FastAPI 호출 → 유사도 기반 상위 상품 리스트만 반환
        // 12/05 FAST API 삭제
        log.info("유사도 기반 상품 조회를 시작합니다");
        List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos =
                imageHashService.rankByImageSimilarity(furnitureTag.getFurnitureUrl(), products, 5);

        // 3-1. 최종반환된 리스트를 기반으로 추천가구 엔티티 저장하고, 큐레이션 결과 저장
        log.info("최종반환된 리스트를 기반으로 큐레이션 결과 저장");
        List<FurnitureProductsInfoResponse.FurnitureProductInfo> responseInfos =
                curationFurnitureService.saveCurationResults(furnitureTag, infos);

        log.info("큐레이션 종료:{}",formatted);
        return FurnitureProductsInfoResponse.of(user.getName(), responseInfos);
    }

    // 기획의사결정용
    @Override
    public FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlan(User user, Long tagId, Long furnitureId, String searchKeyword, int pHash) {
        // 1. FurnitureTag 조회 (DB)
        FurnitureTag furnitureTag = furnitureService.findFurnitureTagForPlan(tagId, furnitureId);

        // 2. 네이버 API 호출
        List<NaverFurnitureProductDto> products = naverShopService.search(searchKeyword, 50);

        // 3. FastAPI 호출 → 유사도 기반 상위 상품 리스트만 반환
        List<FurnitureProductsInfoResponseForPlan.FurnitureProductInfo> infos =
                imageHashService.rankByImageSimilarityForPlan(
                        furnitureTag.getFurnitureUrl(),
                        products,
                        pHash,
                        100-pHash,
                        5);

        // 4. 최종 응답 조립 (Facade 책임)
        return FurnitureProductsInfoResponseForPlan.of(user.getName(), infos);
    }


    @Override
    public FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlanV2(
            User user,
            Long tagId,
            Long furnitureId,
            String searchKeyword,
            int pHash,
            List<String> allowedMalls,
            Boolean applyNaverPay
    ){
        // 1. FurnitureTag 조회 (DB)
        FurnitureTag furnitureTag = furnitureService.findFurnitureTagForPlan(tagId, furnitureId);

        log.info("--------네이버 API 호출을 시작합니다--------");
        // 2. 네이버 API 호출 (V2)
        List<NaverFurnitureProductDto> products = naverShopService.searchV2(
                searchKeyword,
                50,
                (allowedMalls == null || allowedMalls.isEmpty()) ? null : allowedMalls,
                Boolean.TRUE.equals(applyNaverPay) ? "naverpay" : ""
        );

        log.info("--------네이버 API 호출이 완료됐습니다--------");

        if (products == null || products.isEmpty()) {
            throw new GeneralException(ErrorCode.NAVER_RESPONSE_EMPTY);
        }

        // 3. FastAPI 호출 → 유사도 기반 상위 상품 리스트만 반환
        List<FurnitureProductsInfoResponseForPlan.FurnitureProductInfo> infos =
                imageHashService.rankByImageSimilarityForPlan(
                        furnitureTag.getFurnitureUrl(),
                        products,
                        pHash,
                        100-pHash,
                        5
                );

        // 4. 최종 응답 조립 (Facade 책임)
        return FurnitureProductsInfoResponseForPlan.of(user.getName(), infos);
    }
}
