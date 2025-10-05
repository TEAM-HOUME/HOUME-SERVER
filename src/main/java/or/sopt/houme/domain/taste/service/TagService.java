package or.sopt.houme.domain.taste.service;

import or.sopt.houme.domain.taste.entity.Tag;

public interface TagService {

    // userId와 imageId로 1순위 태그 찾기
    Tag findTagByUserIdAndImageId(Long userId, Long imageId);


}
