package or.sopt.houme.domain.taste.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.taste_tag.TasteTagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TasteTagServiceImpl implements TasteTagService {

    private final TasteTagRepository tasteTagRepository;

    // 우선순위 가장 높은 TasteId 반환
    @Override
    public Tag getPriorityId(List<Long> tasteIds) {
        if (tasteIds.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        Tag bestTag = tasteTagRepository.findBestTasteId(tasteIds)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        return bestTag;
    }
}
