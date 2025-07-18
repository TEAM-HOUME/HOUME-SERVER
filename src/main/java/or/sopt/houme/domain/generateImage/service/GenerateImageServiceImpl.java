package or.sopt.houme.domain.generateImage.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.dto.request.IsLikeRequest;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.GenerateImageException;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerateImageServiceImpl implements GenerateImageService {

    private final GenerateImageRepository generateImageRepository;

    // 이미지 생성 서비스
    @Transactional
    @Override
    public GenerateImage createGenerateImage(ImageUploadResponseDTO request, House house) {
        GenerateImage generateImage = GenerateImage.createGenerateImage(request, house);
        return generateImageRepository.save(generateImage);
    }

    @Override
    public GenerateImage findGenerateImage(Long imageId) {

        return generateImageRepository.findById(imageId)
                .orElseThrow(() -> new GenerateImageException(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY));
    }

    // houseId로 생성 이미지 객체 가져오기
    @Override
    public GenerateImage findGenerateImageByHouseId(Long houseId) {
        GenerateImage generateImage = generateImageRepository.findByHouseId(houseId)
                .orElseThrow(() -> new GenerateImageException(ErrorCode.NOT_FOUND_GENERATE_IMAGE_ENTITY));

        return generateImage;
    }
}
