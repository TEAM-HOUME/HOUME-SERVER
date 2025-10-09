package or.sopt.houme.domain.furniture.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.forPlan.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.furniture.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.furniture.service.ImageHashService;
import or.sopt.houme.domain.furniture.service.NaverShopService;
import or.sopt.houme.domain.furniture.service.RecommendFurnitureService;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Component;

import java.util.List;
import or.sopt.houme.global.config.NaverProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class FurnitureFacadeImpl implements FurnitureFacade {

    private final NaverShopService naverShopService;
    private final ImageHashService imageHashService;
    private final FurnitureService furnitureService;
    private final RecommendFurnitureService recommendFurnitureService;
    private final NaverProperties naverProperties;

    @Override
    public FurnitureProductsInfoResponse getFurnitureProductInfoFromNaverApi(User user, Long imageId, Long categoryId) {
        // 1. FurnitureTag 조회 (DB)
        FurnitureTag furnitureTag = furnitureService.findFurnitureTag(user, imageId, categoryId);

        // 2. 네이버 API 호출
        String keyword = furnitureTag.getSearchKeyword();
        List<NaverFurnitureProductDto> products = naverShopService.search(keyword, 100);

        // 3. FastAPI 호출 → 유사도 기반 상위 상품 리스트만 반환
        List<FurnitureProductsInfoResponse.FurnitureProductInfo> infos =
                imageHashService.rankByImageSimilarity(furnitureTag.getFurnitureUrl(), products, 5);

        // 3-1. 최종반환된 리스트를 기반으로 추천가구 엔티티 저장
        recommendFurnitureService.saveRecommendFurniture(infos);

        // 4. 최종 응답 조립 (Facade 책임)
        return FurnitureProductsInfoResponse.of(user.getName(), infos);
    }

    // 기획의사결정용
    @Override
    public FurnitureProductsInfoResponseForPlan getFurnitureProductInfoFromNaverApiForPlan(User user, Long tagId, Long furnitureId, String searchKeyword, int pHash) {
        // 1. FurnitureTag 조회 (DB)
        FurnitureTag furnitureTag = furnitureService.findFurnitureTagForPlan(tagId, furnitureId);

        // 2. 네이버 API 호출
        List<NaverFurnitureProductDto> products = naverShopService.search(searchKeyword, 100);

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
                100,
                (allowedMalls == null || allowedMalls.isEmpty()) ? null : allowedMalls,
                Boolean.TRUE.equals(applyNaverPay) ? "naverpay" : ""
        );

        log.info("--------네이버 API 호출이 완료됐습니다--------");

        if (products == null || products.isEmpty()) {
            throw new GeneralException(ErrorCode.NAVER_RESPONSE_EMPTY);
        }
        log.info(products.get(0).furnitureProductName());

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
