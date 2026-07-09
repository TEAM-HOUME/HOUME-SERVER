package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStyleServiceImpl implements AdminStyleService {

    private final BannerRepository bannerRepository;
    private final AdminBannerSupport adminBannerSupport;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminBannerImageUploadResponse createImageUploadUrl(AdminBannerImageUploadRequest request, String contentType) {
        return adminBannerSupport.createImageUploadUrl(request, contentType, "style");
    }

    @Override
    @Transactional
    public AdminStyleResponse create(AdminStyleCreateRequest request) {
        Map<Long, CurationRawProduct> requiredRawProducts = adminBannerSupport.loadRequiredRawProducts(request.mappedRawProductIds());

        Banner style = Banner.create(
                BannerType.STYLE,
                adminBannerSupport.normalizeRequired(request.bannerImageUrl()),
                adminBannerSupport.normalizeRequired(request.bannerTitle()),
                adminBannerSupport.normalizeRequired(request.styleDescription()),
                null,
                adminBannerSupport.normalizeRequired(request.stylePrompt()),
                null,
                null
        );
        style.replaceRawProducts(adminBannerSupport.buildMappings(style, request.mappedRawProductIds(), requiredRawProducts));

        Banner savedStyle = bannerRepository.saveAndFlush(style);
        return buildResponse(savedStyle, requiredRawProducts);
    }

    @Override
    public AdminStyleListResponse getAll() {
        List<Banner> styles = bannerRepository.findAllWithRawProducts(BannerType.STYLE, false);
        return new AdminStyleListResponse(buildResponses(
                styles.stream()
                        .filter(this::isStyle)
                        .toList()
        ));
    }

    @Override
    public AdminStyleResponse getById(Long styleId) {
        Banner style = bannerRepository.findByIdWithRawProducts(styleId, BannerType.STYLE, false)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_STYLE));
        return buildResponses(List.of(requireStyle(style))).getFirst();
    }

    @Override
    @Transactional
    public AdminStyleResponse update(Long styleId, AdminStyleUpdateRequest request) {
        Banner style = bannerRepository.findByIdWithRawProducts(styleId, BannerType.STYLE, false)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_STYLE));
        requireStyle(style);

        List<Long> targetMappedRawProductIds = request.mappedRawProductIds() != null
                ? request.mappedRawProductIds()
                : adminBannerSupport.extractMappedRawProductIds(style);

        Map<Long, CurationRawProduct> requiredRawProducts = adminBannerSupport.loadRequiredRawProducts(targetMappedRawProductIds);

        style.update(
                BannerType.STYLE,
                request.bannerImageUrl() != null ? adminBannerSupport.normalizeRequired(request.bannerImageUrl()) : style.getBannerImageUrl(),
                request.bannerTitle() != null ? adminBannerSupport.normalizeRequired(request.bannerTitle()) : style.getBannerTitle(),
                request.styleDescription() != null ? adminBannerSupport.normalizeRequired(request.styleDescription()) : style.getStyleDescription(),
                null,
                request.stylePrompt() != null ? adminBannerSupport.normalizeRequired(request.stylePrompt()) : style.getStylePrompt(),
                null,
                null
        );
        style.replaceRawProducts(adminBannerSupport.buildMappings(style, targetMappedRawProductIds, requiredRawProducts));

        Banner savedStyle = bannerRepository.saveAndFlush(style);
        return buildResponse(savedStyle, requiredRawProducts);
    }

    @Override
    @Transactional
    public void delete(Long styleId) {
        Banner style = bannerRepository.findByIdWithRawProducts(styleId, BannerType.STYLE, false)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_STYLE));
        requireStyle(style);
        bannerRepository.delete(style);
        bannerRepository.flush();
    }

    @Override
    public AdminBannerRawProductSearchResponse searchRawProducts(String keyword, int size) {
        return adminBannerSupport.searchRawProducts(keyword, size);
    }

    private List<AdminStyleResponse> buildResponses(List<Banner> styles) {
        Map<Long, CurationRawProduct> rawProductMap = adminBannerSupport.loadRequiredRawProductsForBanners(styles);
        return styles.stream()
                .map(style -> buildResponse(style, rawProductMap))
                .toList();
    }

    private AdminStyleResponse buildResponse(Banner style, Map<Long, CurationRawProduct> rawProductMap) {
        return new AdminStyleResponse(
                style.getId(),
                style.getBannerImageUrl(),
                style.getBannerTitle(),
                style.getStyleDescription(),
                style.getStylePrompt(),
                adminBannerSupport.toMappedRawProductResponses(style, rawProductMap),
                style.getCreatedAt(),
                style.getUpdatedAt()
        );
    }

    private Banner requireStyle(Banner banner) {
        if (!isStyle(banner)) {
            throw new GeneralException(ErrorCode.NOT_FOUND_STYLE);
        }
        return banner;
    }

    private boolean isStyle(Banner banner) {
        return banner != null && BannerType.STYLE.equals(banner.getBannerType());
    }
}
