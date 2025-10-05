package or.sopt.houme.domain.generateImage.service.imageGenerationLog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.entity.ImageGenerationDetail;
import or.sopt.houme.domain.generateImage.entity.ImageGenerationLog;
import or.sopt.houme.domain.generateImage.repository.ImageGenerationDetailRepository;
import or.sopt.houme.domain.generateImage.repository.ImageGenerationLogRepository;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.GenerateImageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationLogServiceImpl implements ImageGenerationLogService {

    private final ImageGenerationLogRepository imageGenerationLogRepository;
    private final ImageGenerationDetailRepository imageGenerationDetailRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public ImageGenerationLog saveImageGenerationLog(Long userId, String abType, int imageCount, List<Taste> tasteList, List<Tag> tagList) {

        // 선택한 무드보드 식별자들
        String selectedMoodboardIds;
        // 선택한 무드보드 이름들
        String selectedMoodboardNames;
        // 선택한 태그 식별자들
        String selectedStyleTagIds;
        // 선택한 태그 이름들
        String selectedStyleTagNames;

        try {
            selectedMoodboardIds = writeValueAsString(tasteList.stream().map(Taste::getId).toList());
            selectedMoodboardNames = writeValueAsString(tasteList.stream().map(Taste::getFilename).toList());
            selectedStyleTagIds = writeValueAsString(tagList.stream().map(Tag::getId).toList());
            selectedStyleTagNames = writeValueAsString(tagList.stream().map(Tag::getTagName).toList());

            ImageGenerationLog build = ImageGenerationLog.builder()
                    .userId(userId)
                    .type(abType)
                    .selectedMoodboardCount(tasteList.size())
                    .generatedImageCount(imageCount)
                    .selectedMoodboardIds(selectedMoodboardIds)
                    .selectedMoodboardNames(selectedMoodboardNames)
                    .selectedStyleTagIds(selectedStyleTagIds)
                    .selectedStyleTagNames(selectedStyleTagNames)
                    .build();

            return imageGenerationLogRepository.save(build);
        } catch (JsonProcessingException e){
            // objectmapper 변환 오류
            log.error(e.getMessage());
            throw new GenerateImageException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    // A/B 요청 이미지 상세 저장
    @Transactional
    @Override
    public void saveImageGenerationDetail(ImageGenerationLog imageGenerationLog, ImageInfoResponse imageInfoResponse, Tag tag) {

        imageGenerationDetailRepository.save(ImageGenerationDetail.builder()
                .imageId(imageInfoResponse.imageId())
                .imageUrl(imageInfoResponse.imageUrl())
                .styleTagName(tag.getTagName())
                .styleTagId(tag.getId())
                .imageGenerationLog(imageGenerationLog)
                .build());
    }

    // json 형태로 변환
    private String writeValueAsString(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
}
