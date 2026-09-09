package or.sopt.houme.compare.infra;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.CoupangCandidate;
import or.sopt.houme.compare.domain.port.out.CoupangSearchPort;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import or.sopt.houme.domain.coupang.repository.CoupangKeywordProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CoupangSearchAdapter implements CoupangSearchPort {

    private final CoupangKeywordProductJpaRepository keywordProductRepository;

    @Override
    public List<CoupangCandidate> findCandidatesByKeyword(String keyword) {
        return keywordProductRepository.findProductsByKeyword(keyword).stream()
                .map(this::toCandidate)
                .collect(Collectors.toList());
    }

    private CoupangCandidate toCandidate(CoupangProductJpaEntity entity) {
        return new CoupangCandidate(
                entity.getName(),
                entity.getImageUrl(),
                entity.getCurrentPrice().doubleValue(),
                entity.getProductUrl(),
                parseEmbedding(entity.getTitleEmbedding()),
                parseEmbedding(entity.getImageEmbedding())
        );
    }

    private static List<Double> parseEmbedding(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            String trimmed = raw.strip();
            if (trimmed.startsWith("[")) trimmed = trimmed.substring(1, trimmed.length() - 1);
            return Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
