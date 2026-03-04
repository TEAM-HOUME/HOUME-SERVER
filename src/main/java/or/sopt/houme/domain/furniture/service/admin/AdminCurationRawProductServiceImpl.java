package or.sopt.houme.domain.furniture.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCurationRawProductServiceImpl implements AdminCurationRawProductService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Pattern SOURCE_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]{0,49}$");

    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminCurationRawProductListResponse getAll(int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Page<CurationRawProduct> pageResult = curationRawProductRepository.findAllByOrderByIdDesc(
                PageRequest.of(normalizedPage, normalizedSize)
        );

        List<AdminCurationRawProductResponse> products = pageResult.getContent()
                .stream()
                .map(AdminCurationRawProductResponse::of)
                .toList();

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
        return AdminCurationRawProductResponse.of(rawProduct);
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
                request.freeShippingCondition()
        );

        try {
            CurationRawProduct saved = curationRawProductRepository.saveAndFlush(rawProduct);
            return AdminCurationRawProductResponse.of(saved);
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
                    request.fetchedAt()
            );
            CurationRawProduct saved = curationRawProductRepository.saveAndFlush(rawProduct);
            return AdminCurationRawProductResponse.of(saved);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT);
        }
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

    private CurationRawProduct getRawProductOrThrow(Long curationRawProductId) {
        return curationRawProductRepository.findById(curationRawProductId)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_CURATION_RAW_PRODUCT));
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
}
