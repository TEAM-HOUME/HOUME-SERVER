package or.sopt.houme.domain.generateImageResult.service;

import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GeneratedImageMetaResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.RelatedImagesResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.SimilarItemsResponse;
import or.sopt.houme.domain.user.model.entity.User;

public interface GenerateImageResultService {

    GenerateImageResultResponse getListResultItems(User user, Long imageId);

    GeneratedImageMetaResponse getGeneratedImageMeta(User user, Long imageId);

    SimilarItemsResponse getSimilarItems(User user, Long imageId);

    RelatedImagesResponse getRelatedImages(User user, Long imageId);
}
