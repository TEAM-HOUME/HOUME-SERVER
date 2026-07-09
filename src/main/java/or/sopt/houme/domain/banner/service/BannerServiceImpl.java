package or.sopt.houme.domain.banner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.presentation.dto.response.BannerDetailAnswerResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.BannerExploreResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.LandingListResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.LandingResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleDetailProductResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleDetailResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleListResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleResponse;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.presentation.dto.response.ProductColorResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.BannerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerServiceImpl implements BannerService {

    private static final TypeReference<List<BannerStyleAnswerChip>> STYLE_ANSWER_CHIP_TYPE = new TypeReference<>() {};

    private final BannerRepository bannerRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;
    private final RecommendFurnitureRepository recommendFurnitureRepository;
    private final JjymRepository jjymRepository;
    private final ObjectMapper objectMapper;

    @Override
    public LandingListResponse getLandings() {
        return LandingListResponse.of(
                bannerRepository.findAllLandingsWithLinkedBanner().stream()
                        .map(LandingResponse::from)
                        .toList()
        );
    }

    @Override
    public BannerExploreListResponse getExploreBanners(Long bannerId) {
        List<Banner> banners = bannerRepository.findAllWithRawProducts(BannerType.BANNER, false).stream()
                .sorted((left, right) -> Long.compare(left.getId(), right.getId()))
                .toList();
        int startIndex = findBannerStartIndex(banners, bannerId);

        List<BannerExploreResponse> orderedBanners = new ArrayList<>(banners.size());
        for (int index = 0; index < banners.size(); index++) {
            Banner banner = banners.get((startIndex + index) % banners.size());
            orderedBanners.add(BannerExploreResponse.from(banner));
        }
        return BannerExploreListResponse.of(orderedBanners);
    }

    @Override
    public BannerDetailResponse getExploreBannerDetail(Long bannerId) {
        Banner banner = bannerRepository.findByIdWithRawProducts(bannerId, BannerType.BANNER, false)
                .orElseThrow(() -> new BannerException(ErrorCode.NOT_FOUND_BANNER));

        return BannerDetailResponse.of(
                banner.getBannerTitle(),
                banner.getBannerImageUrl(),
                banner.getStyleQuestion(),
                parseStyleAnswerChips(banner.getStyleAnswerChipsJson()).stream()
                        .map(chip -> {
                            if (chip.id() == null) {
                                throw new BannerException(ErrorCode.INVALID_BANNER_ANSWER_CHIP);
                            }
                            return BannerDetailAnswerResponse.of(chip.id(), chip.label());
                        })
                        .toList()
        );
    }

    @Override
    public OtherStyleListResponse getOtherStyles(Integer size) {
        if (size != null && size < 1) {
            throw new BannerException(ErrorCode.INVALID_BANNER_SIZE);
        }

        List<OtherStyleResponse> styles = bannerRepository.findAllWithRawProducts(BannerType.STYLE, false).stream()
                .sorted((left, right) -> Long.compare(left.getId(), right.getId()))
                .map(OtherStyleResponse::from)
                .toList();

        if (size == null) {
            return OtherStyleListResponse.of(styles);
        }
        return OtherStyleListResponse.of(styles.stream().limit(size).toList());
    }

    @Override
    public OtherStyleDetailResponse getOtherStyleDetail(User user, Long styleId) {
        Banner style = bannerRepository.findByIdWithRawProducts(styleId, BannerType.STYLE, false)
                .orElseThrow(() -> new BannerException(ErrorCode.NOT_FOUND_STYLE));

        List<CurationRawProduct> rawProducts = style.getBannerRawProducts().stream()
                .sorted((left, right) -> Long.compare(safeMappingId(left), safeMappingId(right)))
                .map(BannerCurationRawProduct::getCurationRawProduct)
                .filter(java.util.Objects::nonNull)
                .toList();
        Map<Long, List<ProductColorResponse>> colorsByRawProductId = buildColorsByRawProductId(rawProducts);
        Set<Long> likedRawProductIds = resolveLikedRawProductIds(user, rawProducts);

        List<OtherStyleDetailProductResponse> products = rawProducts.stream()
                .map(rawProduct -> OtherStyleDetailProductResponse.from(
                        rawProduct,
                        colorsByRawProductId.getOrDefault(rawProduct.getId(), List.of()),
                        likedRawProductIds.contains(rawProduct.getId())
                ))
                .toList();

        return OtherStyleDetailResponse.of(
                style.getBannerTitle(),
                style.getBannerImageUrl(),
                style.getStyleDescription(),
                products
        );
    }

    private int findBannerStartIndex(List<Banner> banners, Long bannerId) {
        for (int index = 0; index < banners.size(); index++) {
            if (banners.get(index).getId().equals(bannerId)) {
                return index;
            }
        }
        throw new BannerException(ErrorCode.NOT_FOUND_BANNER);
    }

    private List<BannerStyleAnswerChip> parseStyleAnswerChips(String styleAnswerChipsJson) {
        if (styleAnswerChipsJson == null || styleAnswerChipsJson.isBlank()) {
            return List.of();
        }
        try {
            List<BannerStyleAnswerChip> chips = objectMapper.readValue(styleAnswerChipsJson, STYLE_ANSWER_CHIP_TYPE);
            if (chips == null) {
                return List.of();
            }
            return chips.stream()
                    .sorted((left, right) -> Integer.compare(left.order(), right.order()))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    private long safeMappingId(BannerCurationRawProduct mapping) {
        if (mapping == null || mapping.getId() == null) {
            return Long.MAX_VALUE;
        }
        return mapping.getId();
    }

    private Map<Long, List<ProductColorResponse>> buildColorsByRawProductId(List<CurationRawProduct> rawProducts) {
        List<Long> rawProductIds = rawProducts.stream()
                .map(CurationRawProduct::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (rawProductIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, LinkedHashSet<String>> colorSetByRawProductId = new LinkedHashMap<>();
        for (CurationRawProductColor color : curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds)) {
            Long rawProductId = color.getCurationRawProduct().getId();
            String colorName = resolveColorName(color);
            if (colorName == null) {
                continue;
            }
            colorSetByRawProductId.computeIfAbsent(rawProductId, ignored -> new LinkedHashSet<>())
                    .add(colorName);
        }

        Map<Long, List<ProductColorResponse>> colorsByRawProductId = new LinkedHashMap<>();
        for (Map.Entry<Long, LinkedHashSet<String>> entry : colorSetByRawProductId.entrySet()) {
            List<ProductColorResponse> colors = entry.getValue().stream()
                    .map(ProductColorResponse::fromName)
                    .toList();
            colorsByRawProductId.put(entry.getKey(), colors);
        }
        return colorsByRawProductId;
    }

    private Set<Long> resolveLikedRawProductIds(User user, List<CurationRawProduct> rawProducts) {
        if (user == null || rawProducts.isEmpty()) {
            return Set.of();
        }

        List<Long> productIds = rawProducts.stream()
                .map(CurationRawProduct::getProductId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return Set.of();
        }

        Map<Long, CurationRawProduct> rawProductByProductId = rawProducts.stream()
                .filter(rawProduct -> rawProduct.getProductId() != null)
                .collect(Collectors.toMap(
                        CurationRawProduct::getProductId,
                        rawProduct -> rawProduct,
                        (left, right) -> left
                ));

        Map<Long, Long> recommendFurnitureIdByProductId = recommendFurnitureRepository
                .findAllBySourceAndFurnitureProductIdIn(CurationSource.RAW, productIds)
                .stream()
                .collect(Collectors.toMap(
                        RecommendFurniture::getFurnitureProductId,
                        RecommendFurniture::getId,
                        (left, right) -> left
                ));

        List<Long> recommendFurnitureIds = recommendFurnitureIdByProductId.values().stream().distinct().toList();
        if (recommendFurnitureIds.isEmpty()) {
            return Set.of();
        }

        Set<Long> likedRecommendFurnitureIds = jjymRepository
                .findAllByUserIdAndRecommendFurnitureIdIn(user.getId(), recommendFurnitureIds)
                .stream()
                .map(Jjym::getRecommendFurniture)
                .filter(java.util.Objects::nonNull)
                .map(RecommendFurniture::getId)
                .collect(Collectors.toSet());

        return recommendFurnitureIdByProductId.entrySet().stream()
                .filter(entry -> likedRecommendFurnitureIds.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(rawProductByProductId::get)
                .filter(java.util.Objects::nonNull)
                .map(CurationRawProduct::getId)
                .collect(Collectors.toSet());
    }

    private String resolveColorName(CurationRawProductColor color) {
        if (color.getClientColorName() != null && !color.getClientColorName().isBlank()) {
            return color.getClientColorName();
        }
        if (color.getRawColorName() != null && !color.getRawColorName().isBlank()) {
            return color.getRawColorName();
        }
        return null;
    }
}
