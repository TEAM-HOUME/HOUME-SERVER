package or.sopt.houme.domain.generateImageResult.service;

import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.user.model.entity.User;

public interface GenerateImageResultService {

    GenerateImageResultResponse getListResultItems(User user, Long imageId);
}
