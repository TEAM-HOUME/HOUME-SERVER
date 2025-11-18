package or.sopt.houme.domain.generateImage.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.service.CreditService;
import or.sopt.houme.domain.generateImage.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.taste.dto.response.TagDTO;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.service.UserService;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateImageTransactionService {

    private final CreditService creditService;
    private final HouseService houseService;
    private final GenerateImageService generateImageService;
    private final UserService userService;

    // DB 관련 로직을 위한 별도의 @Transactional 메서드 생성
    @Transactional
    public List<ImageInfoResponse> saveResultsAndCreateResponse(
            User user, House house, List<ImageUploadResponseDTO> results,
            GenerateImageRequest generateImageRequest, List<TagDTO> priorityIdList, Credit credit) {

        // 크레딧 차감 로직
        creditService.commitCreditDeletion(credit);

        // house에 프롬프트 저장
        for (ImageUploadResponseDTO result : results) {
            houseService.saveHousePrompt(house, result.getPullPrompt());
        }

        // 도면 이미지 생성 및 저장
        List<GenerateImage> generateImages = results.stream()
                .map(result -> generateImageService.createGenerateImage(result, house))
                .toList();

        // 사용자 계정 이미지 생성여부 업데이트
        userService.updateHasGeneratedImage(user);

        // 반환 리스트 생성
        List<ImageInfoResponse> imageInfoResponses = new ArrayList<>();
        for (int i = 0; i < generateImages.size(); i++) {
            imageInfoResponses.add(
                    ImageInfoResponse.of(generateImages.get(i).getId(), generateImages.get(i).getUrl(),
                            generateImageRequest.floorPlan().isMirror(),
                            house.getEquilibrium().getDescription(), house.getForm().getDescription(),
                            priorityIdList.get(i).tagNameKr(), user.getName())
            );
        }
        return imageInfoResponses;
    }
}
