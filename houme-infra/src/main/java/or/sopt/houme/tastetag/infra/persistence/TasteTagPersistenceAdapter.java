package or.sopt.houme.tastetag.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.tag.domain.Tag;
import or.sopt.houme.tag.infra.persistence.TagMapper;
import or.sopt.houme.tastetag.domain.TasteTag;
import or.sopt.houme.tastetag.domain.port.out.TasteTagRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link TasteTagRepositoryPort} 의 JPA/QueryDSL 구현 어댑터.
 */
@Component
@RequiredArgsConstructor
public class TasteTagPersistenceAdapter implements TasteTagRepositoryPort {

    private final TasteTagJpaRepository jpaRepository;
    private final TasteTagQueryRepository queryRepository;

    @Override
    public TasteTag save(TasteTag tasteTag) {
        TasteTagJpaEntity saved = jpaRepository.save(TasteTagMapper.toNewEntity(tasteTag));
        return TasteTagMapper.toDomain(saved);
    }

    @Override
    public void deleteAllByTasteId(Long tasteId) {
        // 기존 동작(조회 후 삭제) 유지 — FK 순서/영속성 컨텍스트 일관성.
        jpaRepository.deleteAll(jpaRepository.findAllByTasteId(tasteId));
    }

    @Override
    public Optional<Tag> findBestTasteId(List<Long> tasteIds) {
        return queryRepository.findBestTasteId(tasteIds).map(TagMapper::toDomain);
    }

    @Override
    public List<Tag> findBestTasteIdList(List<Long> tasteIds) {
        return queryRepository.findBestTasteIdList(tasteIds).stream().map(TagMapper::toDomain).toList();
    }

    @Override
    public List<Tag> findDistinctTagsByTasteIdIn(List<Long> tasteIds) {
        return queryRepository.findDistinctTagsByTasteIdIn(tasteIds).stream().map(TagMapper::toDomain).toList();
    }
}
