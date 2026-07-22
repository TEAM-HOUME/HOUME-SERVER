package or.sopt.houme.tag.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.tag.domain.Tag;
import or.sopt.houme.tag.domain.port.out.TagRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link TagRepositoryPort} 의 JPA/QueryDSL 구현 어댑터.
 */
@Component
@RequiredArgsConstructor
public class TagPersistenceAdapter implements TagRepositoryPort {

    private final TagJpaRepository jpaRepository;
    private final TagQueryRepository queryRepository;

    @Override
    public Optional<Tag> findById(Long tagId) {
        return jpaRepository.findById(tagId).map(TagMapper::toDomain);
    }

    @Override
    public Optional<Tag> findByTagNameKr(String tagNameKr) {
        return jpaRepository.findByTagNameKr(tagNameKr).map(TagMapper::toDomain);
    }

    @Override
    public Optional<Tag> findByPriority(int priority) {
        return jpaRepository.findByPriority(priority).map(TagMapper::toDomain);
    }

    @Override
    public List<Tag> findAll() {
        return jpaRepository.findAll().stream().map(TagMapper::toDomain).toList();
    }

    @Override
    public List<Tag> findAllByOrderByPriorityAsc() {
        return jpaRepository.findAllByOrderByPriorityAsc().stream().map(TagMapper::toDomain).toList();
    }

    @Override
    public Tag save(Tag tag) {
        if (tag.getId() == null) {
            TagJpaEntity saved = jpaRepository.save(TagMapper.toNewEntity(tag));
            return TagMapper.toDomain(saved);
        }
        // 수정: 관리 엔티티를 조회해 필드만 갱신(더티 체킹으로 UPDATE 반영). 못 찾으면 fail-fast.
        TagJpaEntity entity = jpaRepository.findById(tag.getId())
                .orElseThrow(() -> new IllegalStateException("수정할 태그를 찾을 수 없습니다. id=" + tag.getId()));
        entity.apply(tag.getTagName(), tag.getPriority(), tag.getTagNameKr(), tag.getTagPrompt());
        return TagMapper.toDomain(entity);
    }

    @Override
    public void deleteById(Long tagId) {
        jpaRepository.deleteById(tagId);
    }

    @Override
    public Optional<Tag> findTagByUserIdAndImageId(Long userId, Long imageId) {
        return queryRepository.findTagByUserIdAndImageId(userId, imageId).map(TagMapper::toDomain);
    }

    @Override
    public Optional<Tag> findMostFrequentTagByHouseId(Long houseId) {
        return queryRepository.findMostFrequentTagByHouseId(houseId).map(TagMapper::toDomain);
    }

    @Override
    public Optional<Tag> findTagByTasteId(Long tasteId) {
        return queryRepository.findTagByTasteId(tasteId).map(TagMapper::toDomain);
    }
}
