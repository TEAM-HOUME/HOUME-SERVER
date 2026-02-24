package or.sopt.houme.domain.house.service.taste;

import or.sopt.houme.domain.house.model.taste.entity.Tag;

public interface TagService {

    // userId와 imageId로 1순위 태그 찾기
    Tag findTagByUserIdAndImageId(Long userId, Long imageId);

    // 무드보드 Id (tasteId)로 Tag 찾기
    Tag findTagByTasteId(Long tasteId);
}
