package or.sopt.houme.compare.infra;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.CurationCandidate;
import or.sopt.houme.compare.domain.port.out.CurationProductSearchPort;
import or.sopt.houme.compare.infra.repository.CurationProductSearchRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CurationProductSearchAdapter implements CurationProductSearchPort {

    private final CurationProductSearchRepository repository;

    @Override
    public List<CurationCandidate> findCandidates(String category) {
        return repository.findCandidatesByCategory(category).stream()
                .map(this::toCandidate)
                .collect(Collectors.toList());
    }

    private CurationCandidate toCandidate(CurationRawProduct p) {
        return new CurationCandidate(
                p.getSource().toUpperCase(),
                p.getProductName(),
                p.getProductImageUrl(),
                p.getDiscountPrice() != null ? p.getDiscountPrice().doubleValue()
                        : p.getListPrice() != null ? p.getListPrice().doubleValue() : null,
                p.getProductSiteUrl(),
                parseEmbedding(p.getTitleEmbedding()),
                parseEmbedding(p.getImageEmbedding())
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
