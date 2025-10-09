package or.sopt.houme.domain.generateImage.service.imageGenerationLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.dto.SelectedTagInfo;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.entity.ImageGenerationLog;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TagException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationTransactionService {

    private final ImageGenerationLogService imageGenerationLogService;

    @Transactional
    public void saveImageGenerationLog(Long userId, String abType, int imageCount, List<Taste> tasteList, List<Tag> tagList,
                                       List<ImageInfoResponse> imageInfoResponse, List<SelectedTagInfo> selectedTagInfoList) {

        ImageGenerationLog imageGenerationLog = imageGenerationLogService.saveImageGenerationLog(userId, abType, imageCount, tasteList, tagList);

        int i = 0;
        for (ImageInfoResponse response : imageInfoResponse){
            // 같은 태그 값 찾기
            Tag tag = tagList.stream().filter(t -> t.getTagNameKr().equals(response.tagName()))
                    .findAny()
                    .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

            // 상세 로그 저장하기
            imageGenerationLogService.saveImageGenerationDetail(imageGenerationLog, response, tag, selectedTagInfoList.get(i).getSelectionStrategy());

            // i로 태그 선정방식 넣기
            i++;
        }
    }
}
