package or.sopt.houme.domain.taste.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.dto.response.TagDTO;
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

    // 우선순위 가장 높은 2개의 Tag 반환
    @Override
    public List<TagDTO> getPriorityIdList(List<Long> tasteIds) {

        List<Tag> bestTasteIdList = tasteTagRepository.findBestTasteIdList(tasteIds);

        // 태그 리스트가 비어 있다면 예외처리
        if (bestTasteIdList.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY);
        }

        return bestTasteIdList.stream()
                .map(TagDTO::of)
                .toList();
    }

    @Override
    public List<Tag> findDistinctTagsByTasteIds(List<Long> tasteIds) {

        // tasteIds에 해당하는 tasteTag 조회
        return tasteTagRepository.findDistinctTagsByTasteIdIn(tasteIds);
    }
}
