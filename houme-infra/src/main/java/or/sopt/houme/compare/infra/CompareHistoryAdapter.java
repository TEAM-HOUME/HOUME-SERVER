package or.sopt.houme.compare.infra;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.port.out.SaveCompareHistoryPort;
import or.sopt.houme.compare.infra.entity.CompareHistoryJpaEntity;
import or.sopt.houme.compare.infra.repository.CompareHistoryRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompareHistoryAdapter implements SaveCompareHistoryPort {

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
}
