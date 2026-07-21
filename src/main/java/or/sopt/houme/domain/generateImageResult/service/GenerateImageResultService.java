package or.sopt.houme.domain.generateImageResult.service;

import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GeneratedImageMetaResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.RelatedImagesResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.SimilarItemsResponse;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;

public interface GenerateImageResultService {

    GenerateImageResultResponse getListResultItems(UserJpaEntity user, Long imageId);

    GeneratedImageMetaResponse getGeneratedImageMeta(UserJpaEntity user, Long imageId);

    SimilarItemsResponse getSimilarItems(UserJpaEntity user, Long imageId);

    RelatedImagesResponse getRelatedImages(UserJpaEntity user, Long imageId);
}
