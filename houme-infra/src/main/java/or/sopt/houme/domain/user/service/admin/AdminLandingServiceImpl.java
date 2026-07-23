package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLandingServiceImpl implements AdminLandingService {

    private final BannerRepository bannerRepository;
    private final AdminBannerSupport adminBannerSupport;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminBannerImageUploadResponse createImageUploadUrl(AdminBannerImageUploadRequest request, String contentType) {
        return adminBannerSupport.createImageUploadUrl(request, contentType, "landing");
    }

    @Override
    @Transactional
    public AdminLandingResponse create(AdminLandingCreateRequest request) {
        Banner landing = Banner.create(
                BannerType.LANDING,
                adminBannerSupport.normalizeRequired(request.bannerImageUrl()),
                adminBannerSupport.normalizeRequired(request.bannerTitle()),
                null,
                null,
                null,
                null,
                null
        );
        Banner savedLanding = bannerRepository.saveAndFlush(landing);
        return buildResponse(savedLanding);
    }

    @Override
    public AdminLandingListResponse getAll() {
        List<AdminLandingResponse> landings = bannerRepository.findAllWithRawProducts(BannerType.LANDING, false).stream()
                .map(this::buildResponse)
                .toList();
        return new AdminLandingListResponse(landings);
    }

    @Override
    public AdminLandingResponse getById(Long landingId) {
        Banner landing = bannerRepository.findByIdWithRawProducts(landingId, BannerType.LANDING, false)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));
        return buildResponse(landing);
    }

    @Override
    @Transactional
    public AdminLandingResponse update(Long landingId, AdminLandingUpdateRequest request) {
        Banner landing = bannerRepository.findByIdWithRawProducts(landingId, BannerType.LANDING, false)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));

        landing.update(
                BannerType.LANDING,
                request.bannerImageUrl() != null ? adminBannerSupport.normalizeRequired(request.bannerImageUrl()) : landing.getBannerImageUrl(),
                request.bannerTitle() != null ? adminBannerSupport.normalizeRequired(request.bannerTitle()) : landing.getBannerTitle(),
                null,
                null,
                null,
                null,
                null
        );
        Banner savedLanding = bannerRepository.saveAndFlush(landing);
        return buildResponse(savedLanding);
    }

    @Override
    @Transactional
    public void delete(Long landingId) {
        Banner landing = bannerRepository.findByIdWithRawProducts(landingId, BannerType.LANDING, false)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_BANNER));
        bannerRepository.delete(landing);
        bannerRepository.flush();
    }

    private AdminLandingResponse buildResponse(Banner landing) {
        return new AdminLandingResponse(
                landing.getId(),
                landing.getBannerImageUrl(),
                landing.getBannerTitle(),
                landing.getCreatedAt(),
                landing.getUpdatedAt()
        );
    }
}
