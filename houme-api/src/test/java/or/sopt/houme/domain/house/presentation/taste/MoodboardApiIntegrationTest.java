package or.sopt.houme.domain.house.presentation.taste;

import or.sopt.houme.taste.infra.persistence.TasteJpaEntity;
import or.sopt.houme.taste.infra.persistence.TasteJpaRepository;
import or.sopt.houme.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 무드보드 API 계약(정합성) 특성화 테스트 — taste 헥사고날 전환(#582) 전/후로 동일하게 그린이어야 하는 안전망.
 *
 * <p>실제 PostgreSQL 위에서 {@code GET /api/v1/moodboard-images} 를 관통해,
 * 등록된 무드보드가 {@code url→imageUrl / fileExtension} 매핑으로 반환되는 현재 계약을 고정한다.
 */
@Transactional
class MoodboardApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TasteJpaRepository tasteRepository;

    private void seedTaste(String url, String filename, String ext) {
        tasteRepository.save(TasteJpaEntity.builder()
                .url(url)
                .filename(filename)
                .originalFilename(filename)
                .fileExtension(ext)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/moodboard-images 는 등록된 무드보드를 url→imageUrl 매핑으로 반환한다")
    void moodboardImages_returnsRegisteredImages() throws Exception {
        seedTaste("https://img.houme/mb-1.png", "mb-1.png", "png");
        seedTaste("https://img.houme/mb-2.png", "mb-2.png", "png");
        seedTaste("https://img.houme/mb-3.jpg", "mb-3.jpg", "jpg");

        mockMvc.perform(get("/api/v1/moodboard-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.moodBoardResponseList.length()").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.moodBoardResponseList[*].imageUrl",
                        hasItems("https://img.houme/mb-1.png", "https://img.houme/mb-2.png", "https://img.houme/mb-3.jpg")))
                .andExpect(jsonPath("$.data.moodBoardResponseList[*].fileExtension", hasItems("png", "jpg")));
    }
}
