package or.sopt.houme.domain.generateImage.service;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface GenerateImageService {

    // 이미지 생성
    GenerateImage createGenerateImage(ImageUploadResponseDTO request, House house);
}
