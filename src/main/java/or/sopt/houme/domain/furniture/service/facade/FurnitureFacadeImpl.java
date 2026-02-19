package or.sopt.houme.domain.furniture.service.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.forPlan.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.service.CurationFurnitureService;
import or.sopt.houme.domain.furniture.service.CurationRawProductService;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.furniture.service.ImageHashService;
import or.sopt.houme.domain.furniture.service.NaverShopService;
import or.sopt.houme.domain.user.model.entity.User;
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

    private static final int CURATION_LIMIT = 5;
    private static final int RAW_CURATION_LIMIT = 4;

    private final NaverShopService naverShopService;
    private final ImageHashService imageHashService;
    private final FurnitureService furnitureService;
    private final CurationFurnitureService curationFurnitureService;
    private final CurationRawProductService curationRawProductService;
    private final NaverProperties naverProperties;

    @Override
    public FurnitureProductsInfoResponse getFurnitureProductInfoFromNaverApi(User user, Long imageId, Long categoryId) {


        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 1. FurnitureTag 조회 (DB)
        log.info("연관된 가구들을 조회합니다:{}",formatted);
        FurnitureTag furnitureTag = furnitureService.findFurnitureTag(user, imageId, categoryId);

        List<FurnitureProductsInfoResponse.FurnitureProductInfo> naverInfos =
                curationFurnitureService.getCurationProducts(furnitureTag, CurationSource.NAVER);
        if (naverInfos.isEmpty()) {
            log.info("네이버 API 호출을 시작합니다");
            String keyword = furnitureTag.getSearchKeyword();
            List<NaverFurnitureProductDto> products = naverShopService.search(keyword, 50);

            // 2. FastAPI 호출 → 유사도 기반 상위 상품 리스트만 반환
            // 12/05 FAST API 삭제
            log.info("유사도 기반 네이버 상품 조회를 시작합니다");
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> rankedInfos =
                    imageHashService.rankByImageSimilarity(furnitureTag.getFurnitureUrl(), products, CURATION_LIMIT);

            // 2-1. 최종반환된 리스트를 기반으로 추천가구 엔티티 저장하고, 큐레이션 결과 저장
            log.info("네이버 큐레이션 결과 저장");
            naverInfos = curationFurnitureService.saveCurationResults(
                    furnitureTag,
                    rankedInfos,
                    CurationSource.NAVER
            );
        } else {
            log.info("네이버 큐레이션 결과를 DB에서 조회합니다");
        }


        // 기본적인 로직은 naver 로직과 동일합니다
        // 1. furnitrure_tag에 맞는 데이터를 가져옵니다
        List<FurnitureProductsInfoResponse.FurnitureProductInfo> rawInfos =
                curationFurnitureService.getCurationProducts(furnitureTag, CurationSource.RAW);

        // 2. 만약 존재하지 않는다면 그때부터 유사도 계산을 시작합니다
        if (rawInfos.isEmpty()) {
            log.info("RAW 큐레이션 계산을 시작합니다");

            // 2-1. 기존에는 네이버 API로부터 데이터를 불러받아 반환했다면, 이젠 curation_raw_product에서 가져옵니다
            List<NaverFurnitureProductDto> rawCandidates =
                    curationRawProductService.getCandidatesByFurnitureTag(furnitureTag);

            // 2-2. 그 후에 동일한 hash 기반 이미지 유사도를 판별하여 반환합니다
            if (!rawCandidates.isEmpty()) {
                List<FurnitureProductsInfoResponse.FurnitureProductInfo> rankedRawInfos =
                        imageHashService.rankByImageSimilarity(furnitureTag.getFurnitureUrl(), rawCandidates, RAW_CURATION_LIMIT);
                rawInfos = curationFurnitureService.saveCurationResults(
                        furnitureTag,
                        rankedRawInfos,
                        CurationSource.RAW
                );
            } else {
                rawInfos = List.of();
            }
        } else {
            log.info("RAW 큐레이션 결과를 DB에서 조회합니다");
        }

        log.info("큐레이션 종료:{}",formatted);
        return FurnitureProductsInfoResponse.of(
                user.getName(),
                java.util.stream.Stream.concat(naverInfos.stream(), rawInfos.stream()).toList()
        );
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
