package or.sopt.houme.domain.furniture.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ColorHexMapper {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[,/|]");
    private static final Pattern HEX_PATTERN = Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");
    private static final Map<String, String> COLOR_NAME_TO_HEX = buildColorMap();

    private ColorHexMapper() {
    }

    public static List<String> toHexCodes(List<String> colorNames) {
        if (colorNames == null || colorNames.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> hexCodes = new LinkedHashSet<>();
        for (String colorName : colorNames) {
            if (isBlank(colorName)) {
                continue;
            }

            String[] tokens = SPLIT_PATTERN.split(colorName);
            for (String token : tokens) {
                String normalizedToken = token == null ? "" : token.trim();
                if (normalizedToken.isEmpty()) {
                    continue;
                }

                if (isHexCode(normalizedToken)) {
                    hexCodes.add(normalizedToken.toUpperCase());
                    continue;
                }

                String mappedHex = COLOR_NAME_TO_HEX.get(normalizeKey(normalizedToken));
                hexCodes.add(mappedHex != null ? mappedHex : normalizedToken);
            }
        }

        return new ArrayList<>(hexCodes);
    }

    private static boolean isHexCode(String value) {
        return HEX_PATTERN.matcher(value).matches();
    }

    private static String normalizeKey(String value) {
        return value.toLowerCase()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Map<String, String> buildColorMap() {
        Map<String, String> map = new LinkedHashMap<>();

        map.put("white", "#FFFFFF");
        map.put("오프화이트", "#F5F5F5");
        map.put("whiteivory", "#FFF8E1");
        map.put("ivory", "#FFF8E1");
        map.put("아이보리", "#FFF8E1");
        map.put("cream", "#FFFDD0");
        map.put("크림", "#FFFDD0");
        map.put("beige", "#F5F5DC");
        map.put("베이지", "#F5F5DC");

        map.put("black", "#000000");
        map.put("블랙", "#000000");
        map.put("gray", "#808080");
        map.put("grey", "#808080");
        map.put("그레이", "#808080");
        map.put("라이트그레이", "#D3D3D3");
        map.put("lightgray", "#D3D3D3");
        map.put("lightgrey", "#D3D3D3");
        map.put("다크그레이", "#A9A9A9");
        map.put("darkgray", "#A9A9A9");
        map.put("darkgrey", "#A9A9A9");
        map.put("차콜", "#36454F");
        map.put("charcoal", "#36454F");
        map.put("silver", "#C0C0C0");
        map.put("실버", "#C0C0C0");

        map.put("brown", "#8B4513");
        map.put("브라운", "#8B4513");
        map.put("walnut", "#5D4037");
        map.put("월넛", "#5D4037");
        map.put("oak", "#C19A6B");
        map.put("오크", "#C19A6B");
        map.put("acacia", "#B88846");
        map.put("아카시아", "#B88846");
        map.put("wood", "#A67B5B");
        map.put("우드", "#A67B5B");
        map.put("natural", "#D2B48C");
        map.put("내추럴", "#D2B48C");
        map.put("원목", "#8B5A2B");

        map.put("red", "#FF0000");
        map.put("레드", "#FF0000");
        map.put("wine", "#722F37");
        map.put("와인", "#722F37");
        map.put("burgundy", "#800020");
        map.put("버건디", "#800020");
        map.put("pink", "#FFC0CB");
        map.put("핑크", "#FFC0CB");
        map.put("rosepink", "#FF66CC");
        map.put("로즈핑크", "#FF66CC");

        map.put("orange", "#FFA500");
        map.put("오렌지", "#FFA500");
        map.put("yellow", "#FFFF00");
        map.put("옐로우", "#FFFF00");
        map.put("엘로우", "#FFFF00");
        map.put("머스타드", "#FFDB58");
        map.put("mustard", "#FFDB58");

        map.put("green", "#008000");
        map.put("그린", "#008000");
        map.put("olive", "#808000");
        map.put("올리브", "#808000");
        map.put("mint", "#98FF98");
        map.put("민트", "#98FF98");
        map.put("khaki", "#6B8E23");
        map.put("카키", "#6B8E23");

        map.put("blue", "#0000FF");
        map.put("블루", "#0000FF");
        map.put("navy", "#000080");
        map.put("네이비", "#000080");
        map.put("skyblue", "#87CEEB");
        map.put("스카이블루", "#87CEEB");
        map.put("cobalt", "#0047AB");
        map.put("코발트", "#0047AB");
        map.put("ink", "#1F2937");
        map.put("잉크", "#1F2937");
        map.put("딥블루", "#1E3A8A");
        map.put("deepblue", "#1E3A8A");

        map.put("purple", "#800080");
        map.put("퍼플", "#800080");
        map.put("violet", "#8F00FF");
        map.put("바이올렛", "#8F00FF");
        map.put("lavender", "#E6E6FA");
        map.put("라벤더", "#E6E6FA");

        map.put("gold", "#FFD700");
        map.put("골드", "#FFD700");
        map.put("transparent", "#00000000");
        map.put("투명", "#00000000");
        map.put("clear", "#00000000");
        map.put("클리어", "#00000000");

        map.put("화이트", "#FFFFFF");

        return Map.copyOf(map);
    }
}
