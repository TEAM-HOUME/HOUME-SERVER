package or.sopt.houme.domain.taste.repository;


import or.sopt.houme.domain.taste.entity.Tag;

import java.util.Optional;

public interface TagRepositoryCustom {
    Optional<Tag> findTagByUserIdAndImageId(Long userId, Long imageId);
}
