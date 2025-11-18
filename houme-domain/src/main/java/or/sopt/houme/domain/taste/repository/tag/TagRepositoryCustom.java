package or.sopt.houme.domain.taste.repository.tag;


import or.sopt.houme.domain.taste.entity.Tag;

import java.util.Optional;

public interface TagRepositoryCustom {
    Optional<Tag> findTagByUserIdAndImageId(Long userId, Long imageId);

    Optional<Tag> findMostFrequentTagByHouseId(Long houseId);

    // tasteId로 Tag 반환
    Optional<Tag> findTagByTasteId(Long tasteId);
}
