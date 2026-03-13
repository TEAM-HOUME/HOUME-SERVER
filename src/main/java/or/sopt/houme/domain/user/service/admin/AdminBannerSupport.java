package or.sopt.houme.domain.user.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerStyleAnswerChipRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerMappedRawProductResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;
import or.sopt.houme.global.util.S3PresignedUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminBannerSupport {

    private static final int MAX_STYLE_ANSWER_CHIPS = 4;
    private static final int MAX_SEARCH_SIZE = 50;
    private static final TypeReference<List<BannerStyleAnswerChip>> STYLE_ANSWER_CHIP_TYPE = new TypeReference<>() {};

    private final CurationRawProductRepository curationRawProductRepository;
    private final ObjectMapper objectMapper;
    private final S3PresignedUtil s3PresignedUtil;

    public AdminBannerImageUploadResponse createImageUploadUrl(
            AdminBannerImageUploadRequest request,
            String contentType,
            String directory
    ) {
        S3PresignedUrlResponseDTO presignedUrl = s3PresignedUtil.createPresignedUrl(
                normalizeRequired(request.imageExtension()),
                directory,
                contentType
        );
        return new AdminBannerImageUploadResponse(presignedUrl.uploadUrl(), presignedUrl.publicUrl());
    }

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

    public Map<Long, CurationRawProduct> loadRequiredRawProducts(List<Long> rawProductIds) {
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

    public List<Long> extractAllRawProductIds(List<BannerStyleAnswerChip> styleAnswerChips, List<Long> mappedRawProductIds) {
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

    public List<Long> extractMappedRawProductIds(Banner banner) {
        if (banner == null) {
            return List.of();
        }
        return banner.getBannerRawProducts().stream()
                .map(BannerCurationRawProduct::getCurationRawProduct)
                .filter(Objects::nonNull)
                .map(CurationRawProduct::getId)
                .toList();
    }

    public List<BannerStyleAnswerChip> normalizeStyleAnswerChips(List<AdminBannerStyleAnswerChipRequest> requests) {
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

    public String toStyleAnswerChipsJson(List<BannerStyleAnswerChip> chips) {
        try {
            return objectMapper.writeValueAsString(chips == null ? List.of() : chips);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    public List<BannerStyleAnswerChip> parseStyleAnswerChipsJson(String styleAnswerChipsJson) {
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

    public List<BannerCurationRawProduct> buildMappings(
            Banner banner,
            List<Long> mappedRawProductIds,
            Map<Long, CurationRawProduct> rawProductMap
    ) {
        return deduplicateIds(mappedRawProductIds).stream()
                .map(rawProductId -> BannerCurationRawProduct.of(banner, rawProductMap.get(rawProductId)))
                .toList();
    }

    public Map<Long, CurationRawProduct> loadRequiredRawProductsForBanners(List<Banner> banners) {
        return loadRequiredRawProducts(
                banners.stream()
                        .flatMap(banner -> extractAllRawProductIds(
                                parseStyleAnswerChipsJson(banner.getStyleAnswerChipsJson()),
                                extractMappedRawProductIds(banner)
                        ).stream())
                        .toList()
        );
    }

    public List<AdminBannerMappedRawProductResponse> toMappedRawProductResponses(
            Banner banner,
            Map<Long, CurationRawProduct> rawProductMap
    ) {
        return banner.getBannerRawProducts().stream()
                .map(BannerCurationRawProduct::getCurationRawProduct)
                .filter(Objects::nonNull)
                .map(rawProduct -> rawProductMap.getOrDefault(rawProduct.getId(), rawProduct))
                .map(AdminBannerMappedRawProductResponse::of)
                .toList();
    }

    public List<Long> deduplicateIds(List<Long> ids) {
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

    public String normalizeRequired(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return normalized;
    }

    public String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
