package or.sopt.houme.domain.furniture.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class CurationProductTokenizer {

    public String buildTokens(String productName, String brand, List<String> furnitureTypeNames, List<String> customKeywords) {
        Set<String> tokens = new LinkedHashSet<>();

        addWordTokens(tokens, productName);
        addWordTokens(tokens, brand);

        for (String name : furnitureTypeNames == null ? List.<String>of() : furnitureTypeNames) {
            if (name != null && !name.isBlank()) {
                tokens.add(name.trim().toLowerCase());
                addWordTokens(tokens, name);
            }
        }

        for (String kw : customKeywords == null ? List.<String>of() : customKeywords) {
            if (kw != null && !kw.isBlank()) {
                tokens.add(kw.trim().toLowerCase());
                addWordTokens(tokens, kw);
            }
        }

        return String.join(" ", tokens);
    }

    private void addWordTokens(Set<String> tokens, String text) {
        if (text == null || text.isBlank()) return;

        String[] words = text.split("[\\s\\-_/()\\[\\],.]+");
        for (String word : words) {
            String trimmed = word.trim().toLowerCase();
            if (trimmed.isEmpty()) continue;
            tokens.add(trimmed);

            // 한글/비한글 경계에서 복합어 분리
            // ex) "SS매트리스" → "ss" + "매트리스", "3인용소파" → "3인용" + "소파"
            String[] parts = trimmed.split("(?<=[가-힣])(?=[^가-힣])|(?<=[^가-힣])(?=[가-힣])");
            if (parts.length > 1) {
                for (String part : parts) {
                    String p = part.trim();
                    if (!p.isEmpty()) {
                        tokens.add(p);
                    }
                }
            }
        }
    }
}
