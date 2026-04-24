package or.sopt.houme.domain.furniture.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CurationProductTokenizer 단위 테스트")
class CurationProductTokenizerTest {

    private CurationProductTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new CurationProductTokenizer();
    }

    @Test
    @DisplayName("상품명의 공백으로 분리된 단어들이 각각 토큰으로 포함된다")
    void buildTokens_includesSpaceSplitWords() {
        String result = tokenizer.buildTokens("원목 퀸 침대", null, List.of(), List.of());

        assertThat(result).contains("원목");
        assertThat(result).contains("퀸");
        assertThat(result).contains("침대");
    }

    @Test
    @DisplayName("한글/영문 경계의 복합어가 분리된 토큰으로 추가된다")
    void buildTokens_splitsKoreanEnglishBoundary() {
        // "SS매트리스" → "SS" + "매트리스" 모두 포함
        String result = tokenizer.buildTokens("SS매트리스", null, List.of(), List.of());

        assertThat(result).contains("SS매트리스");
        assertThat(result).contains("SS");
        assertThat(result).contains("매트리스");
    }

    @Test
    @DisplayName("숫자/한글 경계의 복합어가 분리된 토큰으로 추가된다")
    void buildTokens_splitsNumberKoreanBoundary() {
        // "3인용소파" → "3인용" + "소파" 모두 포함
        String result = tokenizer.buildTokens("3인용소파", null, List.of(), List.of());

        assertThat(result).contains("3인용소파");
        assertThat(result).contains("소파");
    }

    @Test
    @DisplayName("브랜드명이 토큰에 포함된다")
    void buildTokens_includesBrand() {
        String result = tokenizer.buildTokens("침대 프레임", "이케아", List.of(), List.of());

        assertThat(result).contains("이케아");
    }

    @Test
    @DisplayName("가구 유형명이 토큰에 포함된다")
    void buildTokens_includesFurnitureTypeNames() {
        String result = tokenizer.buildTokens("제품명", null, List.of("침대/프레임", "소파"), List.of());

        assertThat(result).contains("침대/프레임");
        assertThat(result).contains("소파");
    }

    @Test
    @DisplayName("커스텀 키워드가 토큰에 포함된다")
    void buildTokens_includesCustomKeywords() {
        String result = tokenizer.buildTokens("제품명", null, List.of(), List.of("1인소파", "좌식의자"));

        assertThat(result).contains("1인소파");
        assertThat(result).contains("좌식의자");
    }

    @Test
    @DisplayName("null 또는 빈 값은 토큰에 포함되지 않는다")
    void buildTokens_ignoresNullAndBlank() {
        String result = tokenizer.buildTokens(null, "  ", Arrays.asList(null, ""), List.of());

        assertThat(result).isBlank();
    }

    @Test
    @DisplayName("중복 토큰은 한 번만 포함된다")
    void buildTokens_deduplicatesTokens() {
        String result = tokenizer.buildTokens("침대 침대", null, List.of("침대"), List.of("침대"));

        long count = List.of(result.split(" ")).stream()
                .filter("침대"::equals)
                .count();
        assertThat(count).isEqualTo(1);
    }
}
