package or.sopt.houme.domain.house.service.taste;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TagException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;


    @Override
    public Tag findTagByUserIdAndImageId(Long userId, Long imageId) {
        Tag tag = tagRepository.findTagByUserIdAndImageId(userId, imageId)
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        return tag;
    }

    @Override
    public Tag findTagByTasteId(Long tasteId) {
        Tag tag = tagRepository.findTagByTasteId(tasteId)
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        return tag;
    }
}
