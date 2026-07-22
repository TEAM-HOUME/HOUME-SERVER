package or.sopt.houme.domain.house.service.taste;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.tag.domain.Tag;
import or.sopt.houme.tag.domain.port.out.TagRepositoryPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TagException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepositoryPort tagRepositoryPort;


    @Override
    public Tag findTagByUserIdAndImageId(Long userId, Long imageId) {
        Tag tag = tagRepositoryPort.findTagByUserIdAndImageId(userId, imageId)
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        return tag;
    }

    @Override
    public Tag findTagByTasteId(Long tasteId) {
        Tag tag = tagRepositoryPort.findTagByTasteId(tasteId)
                .orElseThrow(() -> new TagException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        return tag;
    }
}
