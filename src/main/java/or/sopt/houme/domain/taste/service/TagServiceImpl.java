package or.sopt.houme.domain.taste.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
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
}
