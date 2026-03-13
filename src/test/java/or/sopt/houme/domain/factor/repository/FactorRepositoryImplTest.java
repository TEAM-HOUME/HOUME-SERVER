package or.sopt.houme.domain.factor.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import or.sopt.houme.domain.preference.model.entity.Factor;
import or.sopt.houme.domain.preference.repository.FactorRepositoryImpl;
import or.sopt.houme.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "external.image-api.base-url=http://localhost:8080",
        "gemini.api-base-url=https://generativelanguage.googleapis.com/v1beta"
})
@Import({FactorRepositoryImpl.class, QuerydslConfig.class})
public class FactorRepositoryImplTest {
    @Autowired
    private FactorRepositoryImpl factorRepositoryImpl;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("isLike=true 조건으로 요인 조회")
    void findFactorsByIsLike_true() {
        // given
        em.persist(Factor.builder().factorText("인테리어가 잘 반영됨").isLike(true).build());
        em.persist(Factor.builder().factorText("가구 배치가 부적절함").isLike(false).build());
        em.flush();
        em.clear();

        // when
        List<Factor> result = factorRepositoryImpl.findFactorsByIsLike(true);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFactorText()).isEqualTo("인테리어가 잘 반영됨");
    }

    @Test
    @DisplayName("isLike=false 조건으로 요인 조회")
    void findFactorsByIsLike_false() {
        // given
        em.persist(Factor.builder().factorText("인테리어가 잘 반영됨").isLike(true).build());
        em.persist(Factor.builder().factorText("이미지에 오류가 있음").isLike(false).build());
        em.flush();
        em.clear();

        // when
        List<Factor> result = factorRepositoryImpl.findFactorsByIsLike(false);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFactorText()).isEqualTo("이미지에 오류가 있음");
    }
}
