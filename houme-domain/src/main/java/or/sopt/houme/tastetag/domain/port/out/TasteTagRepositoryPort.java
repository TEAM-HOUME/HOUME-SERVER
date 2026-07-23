package or.sopt.houme.tastetag.domain.port.out;

import or.sopt.houme.tag.domain.Tag;
import or.sopt.houme.tastetag.domain.TasteTag;

import java.util.List;
import java.util.Optional;

/**
 * 무드보드-태그 매핑 영속화 아웃바운드 포트.
 *
 * <p>매핑 조회는 결과적으로 Tag 를 반환한다(빈도·우선순위 기반). 매핑 자체의 저장/삭제도 담당한다.
 */
public interface TasteTagRepositoryPort {

    TasteTag save(TasteTag tasteTag);

    /** 특정 무드보드(taste)에 연결된 매핑을 모두 제거. */
    void deleteAllByTasteId(Long tasteId);

    /** 무드보드들 중 빈도·우선순위 기준 대표 태그 1건. */
    Optional<Tag> findBestTasteId(List<Long> tasteIds);

    /** 무드보드들 중 빈도 내림차순·우선순위 오름차순 상위 2개 태그. */
    List<Tag> findBestTasteIdList(List<Long> tasteIds);

    /** 무드보드들에 연결된 태그를 우선순위 오름차순으로 중복 없이. */
    List<Tag> findDistinctTagsByTasteIdIn(List<Long> tasteIds);
}
