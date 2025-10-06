package or.sopt.houme.domain.generateImage.service.imageGenerationLog;

import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.entity.ImageGenerationLog;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.entity.Taste;

import java.util.List;

public interface ImageGenerationLogService {

    // 요청 Log 저장
    ImageGenerationLog saveImageGenerationLog(Long userId, String abType, int imageCount, List<Taste> tasteList, List<Tag> tagList);

    // 요청 Image 상세 저장
    void saveImageGenerationDetail(ImageGenerationLog imageGenerationLog, ImageInfoResponse imageInfoResponse, Tag tag);
}
