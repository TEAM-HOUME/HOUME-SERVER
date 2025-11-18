package or.sopt.houme.domain.preference.service;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;

public interface GenerateImagePreferenceService {

    // 좋아요 or 싫어요
    void toggleGenerateImagePreference(GenerateImage generateImage, boolean isLike);

    // 선호도 삭제
    Long deleteGenerateImagePreference(GenerateImage generateImage);
}
