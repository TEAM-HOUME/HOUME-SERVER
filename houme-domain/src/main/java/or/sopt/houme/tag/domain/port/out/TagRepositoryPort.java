package or.sopt.houme.tag.domain.port.out;

import or.sopt.houme.tag.domain.Tag;

import java.util.List;
import java.util.Optional;

/**
 * 태그 영속화 아웃바운드 포트. 도메인/애플리케이션은 이 인터페이스만 알고,
 * 구현(JPA·QueryDSL)은 infra 어댑터가 제공한다.
 */
public interface TagRepositoryPort {

    Optional<Tag> findById(Long tagId);

    Optional<Tag> findByTagNameKr(String tagNameKr);

    Optional<Tag> findByPriority(int priority);

    List<Tag> findAll();

    List<Tag> findAllByOrderByPriorityAsc();

    /** 신규 저장 또는 수정 반영. id 가 null 이면 INSERT, 존재하면 필드 갱신. */
    Tag save(Tag tag);

    /** 삭제. 연관 데이터가 있으면 FK 제약 위반 예외가 전파된다. */
    void deleteById(Long tagId);

    // ---- 클러스터 조인 조회 (기존 TagRepositoryCustom 이관) ----

    /** userId·imageId 기준 1순위 태그. */
    Optional<Tag> findTagByUserIdAndImageId(Long userId, Long imageId);

    /** houseId 기준 최빈 태그. */
    Optional<Tag> findMostFrequentTagByHouseId(Long houseId);

    /** tasteId(무드보드) 기준 태그. */
    Optional<Tag> findTagByTasteId(Long tasteId);
}
