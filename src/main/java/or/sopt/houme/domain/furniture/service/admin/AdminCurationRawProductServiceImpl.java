package or.sopt.houme.domain.furniture.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductExposureUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductFurnitureTagCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductFurnitureTagUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductColorResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductFurnitureTagResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductFurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCurationRawProductServiceImpl implements AdminCurationRawProductService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Pattern SOURCE_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]{0,49}$");

    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;
    private final CurationRawProductFurnitureTagRepository curationRawProductFurnitureTagRepository;
    private final FurnitureTagRepository furnitureTagRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminCurationRawProductListResponse getAll(
            int page,
            int size,
            SoozipCategory category,
            Long minListPrice,
            Long maxListPrice
    ) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        validatePriceRange(minListPrice, maxListPrice);

        Page<CurationRawProduct> pageResult = curationRawProductRepository.findAllByFilters(
                category,
                minListPrice,
                maxListPrice,
                PageRequest.of(normalizedPage, normalizedSize)
        );

        List<AdminCurationRawProductResponse> products = buildResponses(pageResult.getContent());

        return new AdminCurationRawProductListResponse(
                products,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.hasNext(),
                pageResult.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCurationRawProductResponse getById(Long curationRawProductId) {
        CurationRawProduct rawProduct = getRawProductOrThrow(curationRawProductId);
        return buildResponses(List.of(rawProduct)).get(0);
    }

    @Override
    public AdminCurationRawProductResponse create(AdminCurationRawProductCreateRequest request) {
        String source = normalizeSource(request.source());
        SoozipCategory category = request.category();
        Long productId = request.productId();

        validateDuplicate(source, category, productId, null);

        CurationRawProduct rawProduct = CurationRawProduct.of(
                source,
                category,
                productId,
                normalize(request.productImageUrl()),
                normalize(request.productSiteUrl()),
                normalize(request.productName()),
                normalizeNullable(request.productMallName()),
                request.fetchedAt() != null ? request.fetchedAt() : LocalDateTime.now()
        );

        rawProduct.updateMeta(
                normalizeNullable(request.brand()),
                request.listPrice(),
                request.discountRate(),
                request.discountPrice(),
                request.baseShippingFee(),
                request.freeShippingCondition(),
                request.isExposed()
        );

        try {
            CurationRawProduct saved = curationRawProductRepository.saveAndFlush(rawProduct);
            return buildResponses(List.of(saved)).get(0);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT);
        }
    }

    @Override
    public AdminCurationRawProductResponse update(Long curationRawProductId, AdminCurationRawProductUpdateRequest request) {
        CurationRawProduct rawProduct = getRawProductOrThrow(curationRawProductId);

        String targetSource = hasText(request.source())
                ? normalizeSource(request.source())
                : normalizeSource(rawProduct.getSource());
        SoozipCategory targetCategory = request.category() != null ? request.category() : rawProduct.getCategory();
        Long targetProductId = request.productId() != null ? request.productId() : rawProduct.getProductId();

        validateDuplicate(targetSource, targetCategory, targetProductId, rawProduct.getId());

        try {
            rawProduct.updateAdminFields(
                    hasText(request.source()) ? targetSource : null,
                    request.category(),
                    request.productId(),
                    normalizeNullable(request.productImageUrl()),
                    normalizeNullable(request.productSiteUrl()),
                    normalizeNullable(request.productName()),
                    normalizeNullable(request.productMallName()),
                    normalizeNullable(request.brand()),
                    request.listPrice(),
                    request.discountRate(),
                    request.discountPrice(),
                    request.baseShippingFee(),
                    request.freeShippingCondition(),
                    request.fetchedAt(),
                    request.isExposed()
            );
            CurationRawProduct saved = curationRawProductRepository.saveAndFlush(rawProduct);
            return buildResponses(List.of(saved)).get(0);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT);
        }
    }

    @Override
    public void updateExposure(AdminCurationRawProductExposureUpdateRequest request) {
        List<Long> rawProductIds = request.rawProductIds().stream()
                .distinct()
                .toList();

        List<CurationRawProduct> rawProducts = curationRawProductRepository.findAllById(rawProductIds);
        if (rawProducts.size() != rawProductIds.size()) {
            throw new GeneralException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT);
        }

        rawProducts.forEach(rawProduct -> rawProduct.updateExposure(request.isExposed()));
        curationRawProductRepository.saveAll(rawProducts);
        curationRawProductRepository.flush();
    }

    @Override
    public AdminCurationRawProductFurnitureTagResponse createFurnitureTagMapping(
            Long curationRawProductId,
            AdminCurationRawProductFurnitureTagCreateRequest request
    ) {
        CurationRawProduct rawProduct = getRawProductOrThrow(curationRawProductId);
        FurnitureTag furnitureTag = getFurnitureTagOrThrow(request.furnitureTagId());

        if (curationRawProductFurnitureTagRepository.existsByCurationRawProductAndFurnitureTag(rawProduct, furnitureTag)) {
            throw new GeneralException(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT_FURNITURE_TAG);
        }

        CurationRawProductFurnitureTag mapping = CurationRawProductFurnitureTag.of(rawProduct, furnitureTag);
        CurationRawProductFurnitureTag saved = curationRawProductFurnitureTagRepository.saveAndFlush(mapping);
        return AdminCurationRawProductFurnitureTagResponse.of(saved);
    }

    @Override
    public AdminCurationRawProductFurnitureTagResponse updateFurnitureTagMapping(
            Long curationRawProductId,
            Long mappingId,
            AdminCurationRawProductFurnitureTagUpdateRequest request
    ) {
        CurationRawProductFurnitureTag mapping = curationRawProductFurnitureTagRepository
                .findByIdAndCurationRawProductId(mappingId, curationRawProductId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT_FURNITURE_TAG_MAPPING));

        FurnitureTag nextFurnitureTag = getFurnitureTagOrThrow(request.furnitureTagId());
        if (!mapping.getFurnitureTag().getId().equals(nextFurnitureTag.getId())
                && curationRawProductFurnitureTagRepository.existsByCurationRawProductAndFurnitureTag(mapping.getCurationRawProduct(), nextFurnitureTag)) {
            throw new GeneralException(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT_FURNITURE_TAG);
        }

        mapping.updateFurnitureTag(nextFurnitureTag);
        CurationRawProductFurnitureTag saved = curationRawProductFurnitureTagRepository.saveAndFlush(mapping);
        return AdminCurationRawProductFurnitureTagResponse.of(saved);
    }

    @Override
    public void deleteFurnitureTagMapping(Long curationRawProductId, Long mappingId) {
        CurationRawProductFurnitureTag mapping = curationRawProductFurnitureTagRepository
                .findByIdAndCurationRawProductId(mappingId, curationRawProductId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT_FURNITURE_TAG_MAPPING));
        curationRawProductFurnitureTagRepository.delete(mapping);
        curationRawProductFurnitureTagRepository.flush();
    }

    @Override
    public void delete(Long curationRawProductId) {
        CurationRawProduct rawProduct = getRawProductOrThrow(curationRawProductId);

        try {
            curationRawProductColorRepository.deleteAllByCurationRawProduct(rawProduct);
            rawProduct.clearFurnitureTags();
            curationRawProductRepository.delete(rawProduct);
            curationRawProductRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.FOREIGN_KEY_CONSTRAINT_FAIL);
        }
    }

    private List<AdminCurationRawProductResponse> buildResponses(List<CurationRawProduct> rawProducts) {
        if (rawProducts.isEmpty()) {
            return List.of();
        }

        List<Long> rawProductIds = rawProducts.stream()
                .map(CurationRawProduct::getId)
                .toList();

        Map<Long, List<AdminCurationRawProductColorResponse>> colorsByRawProductId = new LinkedHashMap<>();
        List<CurationRawProductColor> colors = curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds);
        for (CurationRawProductColor color : colors) {
            colorsByRawProductId.computeIfAbsent(color.getCurationRawProduct().getId(), key -> new ArrayList<>())
                    .add(AdminCurationRawProductColorResponse.of(color));
        }

        Map<Long, List<AdminCurationRawProductFurnitureTagResponse>> furnitureTagsByRawProductId = new LinkedHashMap<>();
        List<CurationRawProductFurnitureTag> mappings =
                curationRawProductFurnitureTagRepository.findAllByCurationRawProductIdInWithFurnitureTag(rawProductIds);
        for (CurationRawProductFurnitureTag mapping : mappings) {
            furnitureTagsByRawProductId.computeIfAbsent(mapping.getCurationRawProduct().getId(), key -> new ArrayList<>())
                    .add(AdminCurationRawProductFurnitureTagResponse.of(mapping));
        }

        return rawProducts.stream()
                .map(rawProduct -> AdminCurationRawProductResponse.of(
                        rawProduct,
                        colorsByRawProductId.getOrDefault(rawProduct.getId(), List.of()),
                        furnitureTagsByRawProductId.getOrDefault(rawProduct.getId(), List.of())
                ))
                .toList();
    }

    private CurationRawProduct getRawProductOrThrow(Long curationRawProductId) {
        return curationRawProductRepository.findById(curationRawProductId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT));
    }

    private FurnitureTag getFurnitureTagOrThrow(Long furnitureTagId) {
        return furnitureTagRepository.findById(furnitureTagId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_FURNITURE_TAG));
    }

    private void validateDuplicate(String source, SoozipCategory category, Long productId, Long selfId) {
        curationRawProductRepository.findBySourceAndCategoryAndProductId(source, category, productId)
                .filter(existing -> selfId == null || !existing.getId().equals(selfId))
                .ifPresent(existing -> {
                    throw new GeneralException(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT);
                });
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeSource(String source) {
        String normalized = normalize(source);
        if (!hasText(normalized)) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        String canonical = normalized.toLowerCase(Locale.ROOT);
        if (!SOURCE_PATTERN.matcher(canonical).matches()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return canonical;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void validatePriceRange(Long minListPrice, Long maxListPrice) {
        if (minListPrice != null && minListPrice < 0) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        if (maxListPrice != null && maxListPrice < 0) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        if (minListPrice != null && maxListPrice != null && minListPrice > maxListPrice) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }
}
