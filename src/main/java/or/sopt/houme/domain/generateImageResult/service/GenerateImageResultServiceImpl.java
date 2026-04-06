package or.sopt.houme.domain.generateImageResult.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.repository.CurationRawProductFurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageUsedProduct;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.generateImage.repository.GenerateImageUsedProductRepository;
import or.sopt.houme.domain.generateImage.service.GenerateImageService;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultProductResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.RelatedImageResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.RelatedImagesResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.SimilarItemResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.SimilarItemsResponse;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.GenerateImageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerateImageResultServiceImpl implements GenerateImageResultService {

    private final GenerateImageService generateImageService;
    private final GenerateImageRepository generateImageRepository;
    private final BannerRepository bannerRepository;
    private final GenerateImageUsedProductRepository generateImageUsedProductRepository;
    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductFurnitureTagRepository curationRawProductFurnitureTagRepository;
    private final HouseService houseService;

    private static final int RELATED_IMAGE_LIMIT = 10;

    @Override
    public GenerateImageResultResponse getListResultItems(User user, Long imageId) {
        if (user == null) {
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_RESULT_REQUEST);
        }

        GenerateImage generateImage = generateImageService.findGenerateImage(imageId);
        if (generateImage.getResolvedGenerationType() != GenerateImageType.LIST) {
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_TYPE);
        }

        boolean isMirror = resolveIsMirror(generateImage);
        List<GenerateImageResultProductResponse> products = resolveSelectedRawProducts(generateImage).stream()
                .map(GenerateImageResultProductResponse::from)
                .toList();

        return GenerateImageResultResponse.of(
                generateImage.getId(),
                generateImage.getUrl(),
                isMirror,
                products
        );
    }

    @Override
    public SimilarItemsResponse getSimilarItems(User user, Long imageId) {
        if (user == null) {
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_RESULT_REQUEST);
        }

        GenerateImage generateImage = generateImageService.findGenerateImage(imageId);
        if (generateImage.getResolvedGenerationType() != GenerateImageType.LIST) {
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_TYPE);
        }

        List<CurationRawProduct> selectedProducts = resolveSelectedRawProducts(generateImage);
        if (selectedProducts.isEmpty()) {
            return SimilarItemsResponse.of(List.of());
        }

        Set<Long> selectedRawProductIds = selectedProducts.stream()
                .map(CurationRawProduct::getId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (selectedRawProductIds.isEmpty()) {
            return SimilarItemsResponse.of(List.of());
        }

        List<CurationRawProductFurnitureTag> selectedMappings =
                curationRawProductFurnitureTagRepository.findAllByCurationRawProductIdInWithFurnitureTag(List.copyOf(selectedRawProductIds));

        Set<Long> furnitureTypeIds = selectedMappings.stream()
                .map(CurationRawProductFurnitureTag::getFurnitureTag)
                .filter(Objects::nonNull)
                .map(furnitureTag -> furnitureTag.getFurniture())
                .filter(Objects::nonNull)
                .map(furniture -> furniture.getFurnitureType())
                .filter(Objects::nonNull)
                .map(furnitureType -> furnitureType.getId())
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        Set<Long> tagIds = selectedMappings.stream()
                .map(CurationRawProductFurnitureTag::getFurnitureTag)
                .filter(Objects::nonNull)
                .map(furnitureTag -> furnitureTag.getTag())
                .filter(Objects::nonNull)
                .map(tag -> tag.getId())
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        Set<String> brands = selectedProducts.stream()
                .map(CurationRawProduct::getBrand)
                .filter(brand -> brand != null && !brand.isBlank())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        List<CurationRawProduct> recommendedProducts = recommendSimilarProducts(
                furnitureTypeIds,
                tagIds,
                brands,
                selectedRawProductIds
        );

        return SimilarItemsResponse.of(recommendedProducts.stream()
                .map(SimilarItemResponse::from)
                .toList());
    }

    @Override
    public RelatedImagesResponse getRelatedImages(User user, Long imageId) {
        if (user == null) {
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_RESULT_REQUEST);
        }

        GenerateImage generateImage = generateImageService.findGenerateImage(imageId);
        if (generateImage.getResolvedGenerationType() != GenerateImageType.LIST) {
            throw new GenerateImageException(ErrorCode.INVALID_GENERATE_IMAGE_TYPE);
        }

        List<Long> selectedRawProductIds = resolveSelectedRawProducts(generateImage).stream()
                .map(CurationRawProduct::getId)
                .filter(Objects::nonNull)
                .toList();
        if (selectedRawProductIds.isEmpty()) {
            return RelatedImagesResponse.of(user.getDisplayName(), List.of());
        }

        List<RelatedImageResponse> images = generateImageRepository
                .findRelatedImagesByRawProductIds(
                        selectedRawProductIds,
                        generateImage.getId(),
                        RELATED_IMAGE_LIMIT,
                        GenerateImageType.LIST
                )
                .stream()
                .map(related -> RelatedImageResponse.of(
                        related.getId(),
                        related.getUrl(),
                        related.getResolvedGenerationType().name()
                ))
                .toList();

        return RelatedImagesResponse.of(user.getDisplayName(), images);
    }

    private boolean resolveIsMirror(GenerateImage generateImage) {
        if (generateImage.getHouse() == null) {
            return false;
        }
        return houseService.getIsMirrorByHouseId(generateImage.getHouse().getId());
    }

    private List<CurationRawProduct> resolveSelectedRawProducts(GenerateImage generateImage) {
        Banner banner = generateImage.getHouse() != null ? generateImage.getHouse().getBanner() : null;
        if (banner != null) {
            Banner bannerWithRawProducts = bannerRepository.findAllByIdInWithRawProducts(List.of(banner.getId())).stream()
                    .findFirst()
                    .orElse(banner);

            return bannerWithRawProducts.getBannerRawProducts().stream()
                    .sorted((left, right) -> Long.compare(safeMappingId(left), safeMappingId(right)))
                    .map(BannerCurationRawProduct::getCurationRawProduct)
                    .filter(Objects::nonNull)
                    .toList();
        }

        return generateImageUsedProductRepository.findAllByGenerateImageIdInWithRawProduct(List.of(generateImage.getId())).stream()
                .map(GenerateImageUsedProduct::getCurationRawProduct)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<CurationRawProduct> recommendSimilarProducts(
            Set<Long> furnitureTypeIds,
            Set<Long> tagIds,
            Set<String> brands,
            Set<Long> selectedRawProductIds
    ) {
        Map<Long, CurationRawProduct> orderedDistinct = new LinkedHashMap<>();
        List<Long> excludeIds = new ArrayList<>(selectedRawProductIds);

        appendCandidatesByFurnitureType(furnitureTypeIds, excludeIds, orderedDistinct);
        appendCandidatesByTag(tagIds, excludeIds, orderedDistinct);
        appendCandidatesByBrand(brands, excludeIds, orderedDistinct);

        return orderedDistinct.values().stream()
                .limit(4)
                .toList();
    }

    private void appendCandidatesByFurnitureType(
            Set<Long> furnitureTypeIds,
            List<Long> excludeIds,
            Map<Long, CurationRawProduct> orderedDistinct
    ) {
        if (furnitureTypeIds.isEmpty() || orderedDistinct.size() >= 4) {
            return;
        }
        int remainingLimit = Math.max(1, 4 - orderedDistinct.size());
        List<CurationRawProduct> candidates = curationRawProductRepository.findAllSimilarByFurnitureTypeIds(
                List.copyOf(furnitureTypeIds),
                List.copyOf(excludeIds),
                PageRequest.of(0, remainingLimit)
        );
        addCandidates(candidates, excludeIds, orderedDistinct);
    }

    private void appendCandidatesByTag(
            Set<Long> tagIds,
            List<Long> excludeIds,
            Map<Long, CurationRawProduct> orderedDistinct
    ) {
        if (tagIds.isEmpty() || orderedDistinct.size() >= 4) {
            return;
        }
        int remainingLimit = Math.max(1, 4 - orderedDistinct.size());
        List<CurationRawProduct> candidates = curationRawProductRepository.findAllSimilarByTagIds(
                List.copyOf(tagIds),
                List.copyOf(excludeIds),
                PageRequest.of(0, remainingLimit)
        );
        addCandidates(candidates, excludeIds, orderedDistinct);
    }

    private void appendCandidatesByBrand(
            Set<String> brands,
            List<Long> excludeIds,
            Map<Long, CurationRawProduct> orderedDistinct
    ) {
        if (brands.isEmpty() || orderedDistinct.size() >= 4) {
            return;
        }
        int remainingLimit = Math.max(1, 4 - orderedDistinct.size());
        List<CurationRawProduct> candidates = curationRawProductRepository.findAllSimilarByBrands(
                List.copyOf(brands),
                List.copyOf(excludeIds),
                PageRequest.of(0, remainingLimit)
        );
        addCandidates(candidates, excludeIds, orderedDistinct);
    }

    private void addCandidates(
            List<CurationRawProduct> candidates,
            List<Long> excludeIds,
            Map<Long, CurationRawProduct> orderedDistinct
    ) {
        for (CurationRawProduct candidate : candidates) {
            if (candidate == null || candidate.getId() == null) {
                continue;
            }
            if (orderedDistinct.containsKey(candidate.getId())) {
                continue;
            }
            orderedDistinct.put(candidate.getId(), candidate);
            excludeIds.add(candidate.getId());
            if (orderedDistinct.size() >= 4) {
                return;
            }
        }
    }

    private long safeMappingId(BannerCurationRawProduct mapping) {
        if (mapping == null || mapping.getId() == null) {
            return Long.MAX_VALUE;
        }
        return mapping.getId();
    }
}
