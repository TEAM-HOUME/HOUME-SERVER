package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerStyleAnswerChipResponse;
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
public class AdminBannerServiceImpl implements AdminBannerService {

    private final BannerRepository bannerRepository;
    private final AdminBannerSupport adminBannerSupport;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminBannerImageUploadResponse createImageUploadUrl(AdminBannerImageUploadRequest request, String contentType) {
        return adminBannerSupport.createImageUploadUrl(request, contentType, "banner");
    }

    @Override
    @Transactional
    public AdminBannerResponse create(AdminBannerCreateRequest request) {
        List<BannerStyleAnswerChip> styleAnswerChips = adminBannerSupport.normalizeStyleAnswerChips(request.styleAnswerChips());
        Map<Long, CurationRawProduct> requiredRawProducts = adminBannerSupport.loadRequiredRawProducts(
                adminBannerSupport.extractAllRawProductIds(styleAnswerChips, request.mappedRawProductIds())
        );

        Banner banner = Banner.create(
                BannerType.BANNER,
                adminBannerSupport.normalizeRequired(request.bannerImageUrl()),
                adminBannerSupport.normalizeRequired(request.bannerTitle()),
                adminBannerSupport.normalizeRequired(request.styleDescription()),
                adminBannerSupport.normalizeRequired(request.styleQuestion()),
                adminBannerSupport.normalizeRequired(request.stylePrompt()),
                adminBannerSupport.toStyleAnswerChipsJson(styleAnswerChips)
        );
        banner.replaceRawProducts(adminBannerSupport.buildMappings(banner, request.mappedRawProductIds(), requiredRawProducts));
        Banner savedBanner = bannerRepository.saveAndFlush(banner);
        return buildResponse(savedBanner, requiredRawProducts);
    }

    @Override
    public AdminBannerListResponse getAll() {
        List<Banner> banners = bannerRepository.findAllWithRawProducts(BannerType.BANNER, true);
        return new AdminBannerListResponse(buildResponses(banners));
    }

    @Override
    public AdminBannerResponse getById(Long bannerId) {
        Banner banner = bannerRepository.findByIdWithRawProducts(bannerId, BannerType.BANNER, true)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));
        return buildResponses(List.of(banner)).getFirst();
    }

    @Override
    @Transactional
    public AdminBannerResponse update(Long bannerId, AdminBannerUpdateRequest request) {
        Banner banner = bannerRepository.findByIdWithRawProducts(bannerId, BannerType.BANNER, true)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));

        List<BannerStyleAnswerChip> currentChips = adminBannerSupport.parseStyleAnswerChipsJson(banner.getStyleAnswerChipsJson());
        List<BannerStyleAnswerChip> targetChips = request.styleAnswerChips() != null
                ? adminBannerSupport.normalizeStyleAnswerChips(request.styleAnswerChips(), currentChips)
                : currentChips;

        List<Long> currentMappedRawProductIds = adminBannerSupport.extractMappedRawProductIds(banner);
        List<Long> targetMappedRawProductIds = request.mappedRawProductIds() != null
                ? request.mappedRawProductIds()
                : currentMappedRawProductIds;

        Map<Long, CurationRawProduct> requiredRawProducts = adminBannerSupport.loadRequiredRawProducts(
                adminBannerSupport.extractAllRawProductIds(targetChips, targetMappedRawProductIds)
        );

        banner.update(
                BannerType.BANNER,
                request.bannerImageUrl() != null ? adminBannerSupport.normalizeRequired(request.bannerImageUrl()) : banner.getBannerImageUrl(),
                request.bannerTitle() != null ? adminBannerSupport.normalizeRequired(request.bannerTitle()) : banner.getBannerTitle(),
                request.styleDescription() != null ? adminBannerSupport.normalizeRequired(request.styleDescription()) : banner.getStyleDescription(),
                request.styleQuestion() != null ? adminBannerSupport.normalizeRequired(request.styleQuestion()) : banner.getStyleQuestion(),
                request.stylePrompt() != null ? adminBannerSupport.normalizeRequired(request.stylePrompt()) : banner.getStylePrompt(),
                adminBannerSupport.toStyleAnswerChipsJson(targetChips)
        );
        banner.replaceRawProducts(adminBannerSupport.buildMappings(banner, targetMappedRawProductIds, requiredRawProducts));

        Banner savedBanner = bannerRepository.saveAndFlush(banner);
        return buildResponse(savedBanner, requiredRawProducts);
    }

    @Override
    @Transactional
    public void delete(Long bannerId) {
        Banner banner = bannerRepository.findByIdWithRawProducts(bannerId, BannerType.BANNER, true)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));
        bannerRepository.delete(banner);
        bannerRepository.flush();
    }

    @Override
    public AdminBannerRawProductSearchResponse searchRawProducts(String keyword, int size) {
        return adminBannerSupport.searchRawProducts(keyword, size);
    }

    private List<AdminBannerResponse> buildResponses(List<Banner> banners) {
        Map<Long, CurationRawProduct> rawProductMap = adminBannerSupport.loadRequiredRawProductsForBanners(banners);
        return banners.stream()
                .map(banner -> buildResponse(banner, rawProductMap))
                .toList();
    }

    private AdminBannerResponse buildResponse(Banner banner, Map<Long, CurationRawProduct> rawProductMap) {
        List<BannerStyleAnswerChip> styleAnswerChips = adminBannerSupport.parseStyleAnswerChipsJson(banner.getStyleAnswerChipsJson());
        List<AdminBannerStyleAnswerChipResponse> chipResponses = styleAnswerChips.stream()
                .map(chip -> {
                    CurationRawProduct rawProduct = rawProductMap.get(chip.curationRawProductId());
                    return new AdminBannerStyleAnswerChipResponse(
                            chip.id(),
                            chip.order(),
                            chip.label(),
                            chip.selectedPrompt(),
                            chip.curationRawProductId(),
                            rawProduct != null ? rawProduct.getProductName() : null,
                            rawProduct != null ? rawProduct.getProductImageUrl() : null
                    );
                })
                .toList();

        return new AdminBannerResponse(
                banner.getId(),
                banner.getBannerImageUrl(),
                banner.getBannerTitle(),
                banner.getStyleDescription(),
                banner.getStyleQuestion(),
                banner.getStylePrompt(),
                chipResponses,
                adminBannerSupport.toMappedRawProductResponses(banner, rawProductMap),
                banner.getCreatedAt(),
                banner.getUpdatedAt()
        );
    }
}
