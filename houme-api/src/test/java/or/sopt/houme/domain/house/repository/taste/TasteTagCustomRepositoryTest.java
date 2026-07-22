package or.sopt.houme.domain.house.repository.taste;

import jakarta.persistence.EntityManager;
import or.sopt.houme.global.config.QuerydslConfig;
import or.sopt.houme.tag.infra.persistence.TagJpaEntity;
import or.sopt.houme.taste.infra.persistence.TasteJpaEntity;
import or.sopt.houme.tastetag.infra.persistence.TasteTagJpaEntity;
import or.sopt.houme.tastetag.infra.persistence.TasteTagQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TasteTag 클러스터 조인 조회(QueryDSL) 특성화 테스트 — 헥사고날 전환(#582) 안전망.
 *
 * <p>TasteTag→Tag 연관 절단(tagId 참조)으로 로직이 {@link TasteTagQueryRepository} 로 이관됐으며,
 * 빈도·우선순위 기준 Tag 반환 계약을 고정한다. (전환 전 동작을 캡처한 뒤 전환 후에도 동일 보장)
 *
 * <p>시나리오: tagA(priority 1), tagB(priority 2). 무드보드 X→tagB, Y→tagB, Z→tagA (tagB 2회, tagA 1회).
 */
@DataJpaTest
@Import({TasteTagQueryRepository.class, QuerydslConfig.class})
@ActiveProfiles("test")
class TasteTagCustomRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private TasteTagQueryRepository tasteTagQueryRepository;

    private Long tasteXId;
    private Long tasteYId;
    private Long tasteZId;
    private Long tagAId;
    private Long tagBId;

    @BeforeEach
    void setUp() {
        TagJpaEntity tagA = TagJpaEntity.builder().tagName("A").priority(1).tagNameKr("에이").tagPrompt("pA").build();
        TagJpaEntity tagB = TagJpaEntity.builder().tagName("B").priority(2).tagNameKr("비").tagPrompt("pB").build();
        em.persist(tagA);
        em.persist(tagB);
        tagAId = tagA.getId();
        tagBId = tagB.getId();

        TasteJpaEntity x = taste("x");
        TasteJpaEntity y = taste("y");
        TasteJpaEntity z = taste("z");
        em.persist(x);
        em.persist(y);
        em.persist(z);
        tasteXId = x.getId();
        tasteYId = y.getId();
        tasteZId = z.getId();

        em.persist(TasteTagJpaEntity.forInsert(x.getId(), tagB.getId()));
        em.persist(TasteTagJpaEntity.forInsert(y.getId(), tagB.getId()));
        em.persist(TasteTagJpaEntity.forInsert(z.getId(), tagA.getId()));

        em.flush();
        em.clear();
    }

    private TasteJpaEntity taste(String key) {
        return TasteJpaEntity.builder()
                .url("https://test.com/" + key + ".png")
                .filename(key + ".png")
                .originalFilename("origin-" + key + ".png")
                .fileExtension("png")
                .build();
    }

    @Test
    @DisplayName("findDistinctTagsByTasteIdIn 은 무드보드에 연결된 태그를 우선순위 오름차순으로 중복 없이 반환한다")
    void findDistinctTagsByTasteIdIn_ordersByPriorityAsc() {
        List<TagJpaEntity> tags = tasteTagQueryRepository.findDistinctTagsByTasteIdIn(List.of(tasteXId, tasteYId, tasteZId));

        assertThat(tags).hasSize(2);
        assertThat(tags.get(0).getId()).isEqualTo(tagAId); // priority 1 먼저
        assertThat(tags.get(1).getId()).isEqualTo(tagBId);
    }

    @Test
    @DisplayName("findBestTasteIdList 는 빈도 내림차순·우선순위 오름차순으로 상위 태그를 반환한다")
    void findBestTasteIdList_ordersByFrequencyThenPriority() {
        List<TagJpaEntity> tags = tasteTagQueryRepository.findBestTasteIdList(List.of(tasteXId, tasteYId, tasteZId));

        assertThat(tags).isNotEmpty();
        assertThat(tags.get(0).getId()).isEqualTo(tagBId); // 2회로 최빈
    }

    @Test
    @DisplayName("findBestTasteId 는 조건에 맞는 대표 태그 1건을 반환한다")
    void findBestTasteId_returnsOne() {
        Optional<TagJpaEntity> best = tasteTagQueryRepository.findBestTasteId(List.of(tasteXId, tasteYId, tasteZId));

        assertThat(best).isPresent();
    }
}
