package or.sopt.houme.compare.infra;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.CompareHistoryItem;
import or.sopt.houme.compare.domain.port.out.GetCompareHistoryPort;
import or.sopt.houme.compare.domain.port.out.SaveCompareHistoryPort;
import or.sopt.houme.compare.infra.entity.CompareHistoryJpaEntity;
import or.sopt.houme.compare.infra.repository.CompareHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompareHistoryAdapter implements SaveCompareHistoryPort, GetCompareHistoryPort {

    private final CompareHistoryRepository repository;

    @Override
    public void save(Long userId, String sourceUrl, String title, String thumbnail, Long price) {
        repository.save(CompareHistoryJpaEntity.builder()
                .userId(userId)
                .sourceUrl(sourceUrl)
                .title(title)
                .thumbnail(thumbnail)
                .price(price)
                .build());
    }

    @Override
    public List<CompareHistoryItem> findByUserId(Long userId, int limit) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(e -> new CompareHistoryItem(
                        e.getSourceUrl(),
                        e.getThumbnail(),
                        e.getTitle(),
                        e.getPrice(),
                        e.getCreatedAt()
                ))
                .toList();
    }
}
