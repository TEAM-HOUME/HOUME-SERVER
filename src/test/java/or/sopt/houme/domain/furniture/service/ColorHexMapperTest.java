package or.sopt.houme.domain.furniture.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ColorHexMapperTest {

    @Test
    void 색상명을_hex코드로_변환한다() {
        List<String> result = ColorHexMapper.toHexCodes(List.of("화이트", "블랙", "오크"));

        assertThat(result).containsExactly("#FFFFFF", "#000000", "#C19A6B");
    }

    @Test
    void 복합_색상문자열을_분리해서_변환하고_중복을_제거한다() {
        List<String> result = ColorHexMapper.toHexCodes(List.of("화이트, 그린", "화이트/블랙", "그린"));

        assertThat(result).containsExactly("#FFFFFF", "#008000", "#000000");
    }

    @Test
    void 미매핑_색상은_원문으로_fallback하고_기존_hex는_유지한다() {
        List<String> result = ColorHexMapper.toHexCodes(List.of("미정색상", " #ff00aa ", "#00ff00"));

        assertThat(result).containsExactly("미정색상", "#FF00AA", "#00FF00");
    }
}
