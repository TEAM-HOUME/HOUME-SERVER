package or.sopt.houme.domain.user.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerStyleAnswerChipRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerMappedRawProductResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerStyleAnswerChipResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBannerServiceImpl implements AdminBannerService {

    private static final int MAX_STYLE_ANSWER_CHIPS = 4;
    private static final int MAX_SEARCH_SIZE = 50;
    private static final TypeReference<List<BannerStyleAnswerChip>> STYLE_ANSWER_CHIP_TYPE = new TypeReference<>() {};

    private final BannerRepository bannerRepository;
    private final CurationRawProductRepository curationRawProductRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AdminBannerResponse create(AdminBannerCreateRequest request) {
        List<BannerStyleAnswerChip> styleAnswerChips = normalizeStyleAnswerChips(request.styleAnswerChips());
        Map<Long, CurationRawProduct> requiredRawProducts = loadRequiredRawProducts(
                extractAllRawProductIds(styleAnswerChips, request.mappedRawProductIds())
        );

        Banner banner = Banner.create(
                normalizeRequired(request.bannerImageUrl()),
                normalizeRequired(request.bannerTitle()),
                normalizeRequired(request.styleQuestion()),
                normalizeRequired(request.stylePrompt()),
                toStyleAnswerChipsJson(styleAnswerChips),
                request.isExposed()
        );
        banner.replaceRawProducts(buildMappings(banner, request.mappedRawProductIds(), requiredRawProducts));
        Banner savedBanner = bannerRepository.saveAndFlush(banner);
        Banner savedWithRawProducts = bannerRepository.findByIdWithRawProducts(savedBanner.getId())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));
        return buildResponse(savedWithRawProducts, requiredRawProducts);
    }

    @Override
    public AdminBannerListResponse getAll() {
        List<Banner> banners = bannerRepository.findAllWithRawProducts();
        return new AdminBannerListResponse(buildResponses(banners));
    }

    @Override
    public AdminBannerResponse getById(Long bannerId) {
        Banner banner = bannerRepository.findByIdWithRawProducts(bannerId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));
        return buildResponses(List.of(banner)).getFirst();
    }

    @Override
    @Transactional
    public AdminBannerResponse update(Long bannerId, AdminBannerUpdateRequest request) {
        Banner banner = bannerRepository.findByIdWithRawProducts(bannerId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));

        List<BannerStyleAnswerChip> currentChips = parseStyleAnswerChipsJson(banner.getStyleAnswerChipsJson());
        List<BannerStyleAnswerChip> targetChips = request.styleAnswerChips() != null
                ? normalizeStyleAnswerChips(request.styleAnswerChips())
                : currentChips;

        List<Long> currentMappedRawProductIds = banner.getBannerRawProducts().stream()
                .map(BannerCurationRawProduct::getCurationRawProduct)
                .filter(Objects::nonNull)
                .map(CurationRawProduct::getId)
                .toList();
        List<Long> targetMappedRawProductIds = request.mappedRawProductIds() != null
                ? request.mappedRawProductIds()
                : currentMappedRawProductIds;

        Map<Long, CurationRawProduct> requiredRawProducts = loadRequiredRawProducts(
                extractAllRawProductIds(targetChips, targetMappedRawProductIds)
        );

        banner.update(
                request.bannerImageUrl() != null ? normalizeRequired(request.bannerImageUrl()) : banner.getBannerImageUrl(),
                request.bannerTitle() != null ? normalizeRequired(request.bannerTitle()) : banner.getBannerTitle(),
                request.styleQuestion() != null ? normalizeRequired(request.styleQuestion()) : banner.getStyleQuestion(),
                request.stylePrompt() != null ? normalizeRequired(request.stylePrompt()) : banner.getStylePrompt(),
                toStyleAnswerChipsJson(targetChips),
                request.isExposed() != null ? request.isExposed() : banner.getIsExposed()
        );
        banner.replaceRawProducts(buildMappings(banner, targetMappedRawProductIds, requiredRawProducts));

        Banner savedBanner = bannerRepository.saveAndFlush(banner);
        Banner savedWithRawProducts = bannerRepository.findByIdWithRawProducts(savedBanner.getId())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));
        return buildResponse(savedWithRawProducts, requiredRawProducts);
    }

    @Override
    @Transactional
    public void delete(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));
        bannerRepository.delete(banner);
        bannerRepository.flush();
    }

    @Override
    public AdminBannerRawProductSearchResponse searchRawProducts(String keyword, int size) {
        String normalizedKeyword = normalizeOptional(keyword);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_SEARCH_SIZE);
        List<AdminBannerMappedRawProductResponse> rawProducts = curationRawProductRepository.searchByKeyword(
                        normalizedKeyword,
                        PageRequest.of(0, normalizedSize)
                ).getContent().stream()
                .map(AdminBannerMappedRawProductResponse::of)
                .toList();
        return new AdminBannerRawProductSearchResponse(rawProducts);
    }

    private List<AdminBannerResponse> buildResponses(List<Banner> banners) {
        Map<Long, CurationRawProduct> rawProductMap = loadRequiredRawProducts(
                banners.stream()
                        .flatMap(banner -> extractAllRawProductIds(
                                parseStyleAnswerChipsJson(banner.getStyleAnswerChipsJson()),
                                banner.getBannerRawProducts().stream()
                                        .map(BannerCurationRawProduct::getCurationRawProduct)
                                        .filter(Objects::nonNull)
                                        .map(CurationRawProduct::getId)
                                        .toList()
                        ).stream())
                        .toList()
        );
        return banners.stream()
                .map(banner -> buildResponse(banner, rawProductMap))
                .toList();
    }

    private AdminBannerResponse buildResponse(Banner banner, Map<Long, CurationRawProduct> rawProductMap) {
        List<BannerStyleAnswerChip> styleAnswerChips = parseStyleAnswerChipsJson(banner.getStyleAnswerChipsJson());
        List<AdminBannerStyleAnswerChipResponse> chipResponses = styleAnswerChips.stream()
                .map(chip -> {
                    CurationRawProduct rawProduct = rawProductMap.get(chip.curationRawProductId());
                    return new AdminBannerStyleAnswerChipResponse(
                            chip.order(),
                            chip.label(),
                            chip.curationRawProductId(),
                            rawProduct != null ? rawProduct.getProductName() : null,
                            rawProduct != null ? rawProduct.getProductImageUrl() : null
                    );
                })
                .toList();

        List<AdminBannerMappedRawProductResponse> mappedRawProducts = banner.getBannerRawProducts().stream()
                .map(BannerCurationRawProduct::getCurationRawProduct)
                .filter(Objects::nonNull)
                .map(rawProduct -> rawProductMap.getOrDefault(rawProduct.getId(), rawProduct))
                .map(AdminBannerMappedRawProductResponse::of)
                .toList();

        return new AdminBannerResponse(
                banner.getId(),
                banner.getBannerImageUrl(),
                banner.getBannerTitle(),
                banner.getStyleQuestion(),
                banner.getStylePrompt(),
                banner.getIsExposed(),
                chipResponses,
                mappedRawProducts,
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }

    private List<BannerCurationRawProduct> buildMappings(
            Banner banner,
            List<Long> mappedRawProductIds,
            Map<Long, CurationRawProduct> rawProductMap
    ) {
        return deduplicateIds(mappedRawProductIds).stream()
                .map(rawProductId -> BannerCurationRawProduct.of(banner, rawProductMap.get(rawProductId)))
                .toList();
    }

    private Map<Long, CurationRawProduct> loadRequiredRawProducts(List<Long> rawProductIds) {
        List<Long> normalizedRawProductIds = deduplicateIds(rawProductIds);
        if (normalizedRawProductIds.isEmpty()) {
            return Map.of();
        }

        List<CurationRawProduct> rawProducts = curationRawProductRepository.findAllById(normalizedRawProductIds);
        Map<Long, CurationRawProduct> rawProductMap = rawProducts.stream()
                .collect(Collectors.toMap(CurationRawProduct::getId, rawProduct -> rawProduct, (left, right) -> left, LinkedHashMap::new));

        if (rawProductMap.size() != normalizedRawProductIds.size()) {
            throw new GeneralException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT);
        }
        return rawProductMap;
    }

    private List<Long> extractAllRawProductIds(List<BannerStyleAnswerChip> styleAnswerChips, List<Long> mappedRawProductIds) {
        List<Long> rawProductIds = new ArrayList<>();
        if (styleAnswerChips != null) {
            rawProductIds.addAll(styleAnswerChips.stream()
                    .map(BannerStyleAnswerChip::curationRawProductId)
                    .toList());
        }
        if (mappedRawProductIds != null) {
            rawProductIds.addAll(mappedRawProductIds);
        }
        return rawProductIds;
    }

    private List<BannerStyleAnswerChip> normalizeStyleAnswerChips(List<AdminBannerStyleAnswerChipRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        if (requests.size() > MAX_STYLE_ANSWER_CHIPS) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        List<BannerStyleAnswerChip> chips = requests.stream()
                .map(request -> new BannerStyleAnswerChip(
                        request.order(),
                        normalizeRequired(request.label()),
                        request.curationRawProductId()
                ))
                .sorted(Comparator.comparing(BannerStyleAnswerChip::order))
                .toList();

        Set<Integer> orders = new LinkedHashSet<>();
        for (BannerStyleAnswerChip chip : chips) {
            if (chip.order() == null || chip.order() < 1) {
                throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }
            if (!orders.add(chip.order())) {
                throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }
            if (chip.curationRawProductId() == null) {
                throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
            }
        }
        return chips;
    }

    private String toStyleAnswerChipsJson(List<BannerStyleAnswerChip> chips) {
        try {
            return objectMapper.writeValueAsString(chips == null ? List.of() : chips);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    private List<BannerStyleAnswerChip> parseStyleAnswerChipsJson(String styleAnswerChipsJson) {
        if (styleAnswerChipsJson == null || styleAnswerChipsJson.isBlank()) {
            return List.of();
        }
        try {
            List<BannerStyleAnswerChip> chips = objectMapper.readValue(styleAnswerChipsJson, STYLE_ANSWER_CHIP_TYPE);
            return chips == null ? List.of() : chips.stream()
                    .sorted(Comparator.comparing(BannerStyleAnswerChip::order))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    private List<Long> deduplicateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new
                ));
    }

    private String normalizeRequired(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
