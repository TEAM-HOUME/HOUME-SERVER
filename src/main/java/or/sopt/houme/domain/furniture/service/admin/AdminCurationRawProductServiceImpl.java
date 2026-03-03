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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCurationRawProductServiceImpl implements AdminCurationRawProductService {

    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminCurationRawProductListResponse getAll() {
        List<AdminCurationRawProductResponse> products = curationRawProductRepository.findAllByOrderByIdDesc()
                .stream()
                .map(AdminCurationRawProductResponse::of)
                .toList();

        return new AdminCurationRawProductListResponse(products);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCurationRawProductResponse getById(Long curationRawProductId) {
        CurationRawProduct rawProduct = getRawProductOrThrow(curationRawProductId);
        return AdminCurationRawProductResponse.of(rawProduct);
    }

    @Override
    public AdminCurationRawProductResponse create(AdminCurationRawProductCreateRequest request) {
        String source = normalize(request.source());
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
            CurationRawProduct saved = curationRawProductRepository.save(rawProduct);
            return AdminCurationRawProductResponse.of(saved);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT);
        }
    }

    @Override
    public AdminCurationRawProductResponse update(Long curationRawProductId, AdminCurationRawProductUpdateRequest request) {
        CurationRawProduct rawProduct = getRawProductOrThrow(curationRawProductId);

        String targetSource = hasText(request.source()) ? normalize(request.source()) : rawProduct.getSource();
        SoozipCategory targetCategory = request.category() != null ? request.category() : rawProduct.getCategory();
        Long targetProductId = request.productId() != null ? request.productId() : rawProduct.getProductId();

        validateDuplicate(targetSource, targetCategory, targetProductId, rawProduct.getId());

        rawProduct.updateAdminFields(
                normalizeNullable(request.source()),
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

        return AdminCurationRawProductResponse.of(rawProduct);
    }

    @Override
    public void delete(Long curationRawProductId) {
        CurationRawProduct rawProduct = getRawProductOrThrow(curationRawProductId);

        try {
            curationRawProductColorRepository.deleteAllByCurationRawProduct(rawProduct);
            rawProduct.clearFurnitureTags();
            curationRawProductRepository.delete(rawProduct);
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
