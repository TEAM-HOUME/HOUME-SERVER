package or.sopt.houme.domain.generateImage.service;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface GenerateImageService {

    // 이미지 생성
    GenerateImage createGenerateImage(ImageUploadResponseDTO request, House house);

    // 이미지 ID로 조회
    GenerateImage findGenerateImage(Long imageId);

    // houseId로 이미지 조회
    GenerateImage findGenerateImageByHouseId(Long houseId);
}
