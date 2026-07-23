package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.service.ColorHexMapper;

import java.util.List;

public record ProductColorResponse(
        String name,
        String value
) {
    public static ProductColorResponse fromName(String colorName) {
        String hexValue = null;
        List<String> mapped = ColorHexMapper.toHexCodes(List.of(colorName));
        if (!mapped.isEmpty() && mapped.getFirst() != null && mapped.getFirst().startsWith("#")) {
            hexValue = mapped.getFirst();
        }
        return new ProductColorResponse(colorName, hexValue);
    }
}
