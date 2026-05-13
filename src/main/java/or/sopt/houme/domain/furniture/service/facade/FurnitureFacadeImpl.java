package or.sopt.houme.domain.furniture.service.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.forPlan.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop.NaverFurnitureProductDto;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureProductsInfoResponseV2;
import or.sopt.houme.domain.furniture.presentation.dto.response.ProductColorResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;
    private final JjymRepository jjymRepository;
    private final NaverProperties naverProperties;

    @Override
    public FurnitureProductsInfoResponseV2 getFurnitureProductInfoFromNaverApi(User user, Long imageId, Long categoryId) {


        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 1. FurnitureTag 조회 (DB)
        log.info("연관된 가구들을 조회합니다:{}",formatted);
        FurnitureTag furnitureTag = furnitureService.findFurnitureTag(user, imageId, categoryId);

        // 네이버 큐레이션은 현재 비활성화합니다.
        // 기존 로직은 추후 복구를 위해 주석으로 유지합니다.
//        List<FurnitureProductsInfoResponse.FurnitureProductInfo> naverInfos =
//                curationFurnitureService.getCurationProducts(furnitureTag, CurationSource.NAVER);
//        if (naverInfos.isEmpty()) {
//            log.info("네이버 API 호출을 시작합니다");
//            String keyword = furnitureTag.getSearchKeyword();
//            List<NaverFurnitureProductDto> products = naverShopService.search(keyword, 50);
//
//            // 2. FastAPI 호출 → 유사도 기반 상위 상품 리스트만 반환
//            // 12/05 FAST API 삭제
//            log.info("유사도 기반 네이버 상품 조회를 시작합니다");
//            List<FurnitureProductsInfoResponse.FurnitureProductInfo> rankedInfos =
//                    imageHashService.rankByImageSimilarity(furnitureTag.getFurnitureUrl(), products, CURATION_LIMIT);
//
//            // 2-1. 최종반환된 리스트를 기반으로 추천가구 엔티티 저장하고, 큐레이션 결과 저장
//            log.info("네이버 큐레이션 결과 저장");
//            naverInfos = curationFurnitureService.saveCurationResults(
//                    furnitureTag,
//                    rankedInfos,
//                    CurationSource.NAVER
//            );
//        } else {
//            log.info("네이버 큐레이션 결과를 DB에서 조회합니다");
//        }


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

        String categoryName = furnitureTag.getFurniture() != null ? furnitureTag.getFurniture().getFurnitureNameKr() : null;
        Map<Long, CurationRawProduct> rawProductByProductId = findLatestRawProductByProductId(furnitureTag, rawInfos);
        Map<Long, List<ProductColorResponse>> colorsByRawProductId = findColorMapByRawProductId(rawProductByProductId);

        List<FurnitureProductsInfoResponseV2.ProductWrapper> products = rawInfos.stream()
                .map(info -> {
                    CurationRawProduct rawProduct = rawProductByProductId.get(info.furnitureProductId());
                    Long rawProductId = rawProduct != null ? rawProduct.getId() : null;

                    FurnitureProductsInfoResponseV2.ProductInfo product = new FurnitureProductsInfoResponseV2.ProductInfo(
                            rawProductId,
                            info.furnitureProductId(),
                            categoryName,
                            rawProduct != null ? rawProduct.getSource() : CurationSource.RAW.name().toLowerCase(),
                            rawProduct != null ? rawProduct.getBrand() : info.brandName(),
                            rawProduct != null ? rawProduct.getProductName() : info.furnitureProductName(),
                            rawProduct != null ? rawProduct.getProductImageUrl() : info.furnitureProductImageUrl(),
                            rawProduct != null ? rawProduct.getListPrice() : info.listPrice(),
                            rawProduct != null ? rawProduct.getDiscountRate() : info.discountRate(),
                            rawProduct != null ? rawProduct.getDiscountPrice() : info.discountPrice(),
                            rawProduct != null ? rawProduct.getProductMallName() : info.furnitureProductMallName(),
                            rawProduct != null ? rawProduct.getProductSiteUrl() : info.furnitureProductSiteUrl(),
                            rawProductId != null ? colorsByRawProductId.getOrDefault(rawProductId, List.of()) : List.of(),
                            info.id() != null && jjymRepository.existsByUserIdAndRecommendFurnitureId(user.getId(), info.id())
                    );
                    return FurnitureProductsInfoResponseV2.ProductWrapper.of(product);
                })
                .toList();

        return FurnitureProductsInfoResponseV2.of(user.getName(), products);
    }

    private Map<Long, CurationRawProduct> findLatestRawProductByProductId(
            FurnitureTag furnitureTag,
            List<FurnitureProductsInfoResponse.FurnitureProductInfo> rawInfos
    ) {
        List<Long> productIds = rawInfos.stream()
                .map(FurnitureProductsInfoResponse.FurnitureProductInfo::furnitureProductId)
                .filter(productId -> productId != null)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return Map.of();
        }

        return curationRawProductRepository.findAllByFurnitureTagAndProductIdIn(furnitureTag, productIds).stream()
                .collect(Collectors.toMap(
                        CurationRawProduct::getProductId,
                        rawProduct -> rawProduct,
                        this::selectLatestRawProduct,
                        LinkedHashMap::new
                ));
    }

    private Map<Long, List<ProductColorResponse>> findColorMapByRawProductId(Map<Long, CurationRawProduct> rawProductByProductId) {
        if (rawProductByProductId.isEmpty()) {
            return Map.of();
        }

        List<Long> rawProductIds = rawProductByProductId.values().stream()
                .map(CurationRawProduct::getId)
                .toList();

        Map<Long, Set<String>> colorNamesByRawProductId = new LinkedHashMap<>();
        List<CurationRawProductColor> colorEntities = curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds);
        for (CurationRawProductColor colorEntity : colorEntities) {
            Long rawProductId = colorEntity.getCurationRawProduct().getId();
            if (rawProductId == null) {
                continue;
            }

            String colorName = resolveColorName(colorEntity);
            if (colorName == null) {
                continue;
            }
            colorNamesByRawProductId.computeIfAbsent(rawProductId, key -> new java.util.LinkedHashSet<>())
                    .add(colorName);
        }

        Map<Long, List<ProductColorResponse>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Set<String>> entry : colorNamesByRawProductId.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream()
                    .map(ProductColorResponse::fromName)
                    .toList());
        }
        return result;
    }

    private CurationRawProduct selectLatestRawProduct(CurationRawProduct current, CurationRawProduct candidate) {
        LocalDateTime currentFetchedAt = current.getFetchedAt();
        LocalDateTime candidateFetchedAt = candidate.getFetchedAt();

        if (currentFetchedAt == null && candidateFetchedAt == null) {
            if (candidate.getId() != null && current.getId() != null && candidate.getId() > current.getId()) {
                return candidate;
            }
            return current;
        }
        if (currentFetchedAt == null) {
            return candidate;
        }
        if (candidateFetchedAt == null) {
            return current;
        }
        if (candidateFetchedAt.isAfter(currentFetchedAt)) {
            return candidate;
        }
        if (candidateFetchedAt.isEqual(currentFetchedAt)
                && candidate.getId() != null
                && current.getId() != null
                && candidate.getId() > current.getId()) {
            return candidate;
        }
        return current;
    }

    private String resolveColorName(CurationRawProductColor colorEntity) {
        if (colorEntity.getClientColorName() != null && !colorEntity.getClientColorName().isBlank()) {
            return colorEntity.getClientColorName();
        }
        if (colorEntity.getRawColorName() != null && !colorEntity.getRawColorName().isBlank()) {
            return colorEntity.getRawColorName();
        }
        return null;
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
