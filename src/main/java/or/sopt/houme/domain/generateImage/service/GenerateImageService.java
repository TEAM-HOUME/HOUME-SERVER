package or.sopt.houme.domain.generateImage.service;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.transaction.annotation.Transactional;

public interface GenerateImageService {

    // 이미지 생성
    @Transactional
    GenerateImage createGenerateImage(ImageUploadResponseDTO request, House house);

    @Transactional
    GenerateImage createGenerateImage(
            ImageUploadResponseDTO request,
            House house,
            GenerateImageType generationType
    );

    // 이미지 ID로 조회
    GenerateImage findGenerateImage(Long imageId);

    // houseId로 이미지 조회
    GenerateImage findGenerateImageByHouseId(Long houseId);
}
