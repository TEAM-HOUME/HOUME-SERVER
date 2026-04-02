package or.sopt.houme.domain.generateImageResult.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageUsedProduct;
import or.sopt.houme.domain.generateImage.repository.GenerateImageUsedProductRepository;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultProductResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.ValidException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerateImageResultServiceImpl implements GenerateImageResultService {

    private final GenerateImageService generateImageService;
    private final BannerRepository bannerRepository;
    private final GenerateImageUsedProductRepository generateImageUsedProductRepository;
    private final HouseService houseService;

    @Override
    public GenerateImageResultResponse getListResultItems(User user, Long imageId) {
        if (user == null) {
            throw new ValidException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        GenerateImage generateImage = generateImageService.findGenerateImage(imageId);
        if (generateImage.getResolvedGenerationType() != GenerateImageType.LIST) {
            throw new ValidException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        boolean isMirror = resolveIsMirror(generateImage);
        List<GenerateImageResultProductResponse> products = resolveProducts(generateImage);

        return GenerateImageResultResponse.of(
                generateImage.getId(),
                generateImage.getUrl(),
                isMirror,
                products
        );
    }

    private boolean resolveIsMirror(GenerateImage generateImage) {
        if (generateImage.getHouse() == null) {
            return false;
        }
        return houseService.getIsMirrorByHouseId(generateImage.getHouse().getId());
    }

    private List<GenerateImageResultProductResponse> resolveProducts(GenerateImage generateImage) {
        Banner banner = generateImage.getBanner();
        if (banner != null) {
            Banner bannerWithRawProducts = bannerRepository.findAllByIdInWithRawProducts(List.of(banner.getId())).stream()
                    .findFirst()
                    .orElse(banner);

            return bannerWithRawProducts.getBannerRawProducts().stream()
                    .sorted((left, right) -> Long.compare(safeMappingId(left), safeMappingId(right)))
                    .map(BannerCurationRawProduct::getCurationRawProduct)
                    .filter(Objects::nonNull)
                    .map(GenerateImageResultProductResponse::from)
                    .toList();
        }

        return generateImageUsedProductRepository.findAllByGenerateImageIdInWithRawProduct(List.of(generateImage.getId())).stream()
                .map(GenerateImageUsedProduct::getCurationRawProduct)
                .filter(Objects::nonNull)
                .map(GenerateImageResultProductResponse::from)
                .toList();
    }

    private long safeMappingId(BannerCurationRawProduct mapping) {
        if (mapping == null || mapping.getId() == null) {
            return Long.MAX_VALUE;
        }
        return mapping.getId();
    }
}
