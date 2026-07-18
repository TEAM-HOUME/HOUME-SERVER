package or.sopt.houme.taste.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.taste.domain.Taste;
import or.sopt.houme.taste.domain.port.out.TasteRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link TasteRepositoryPort} 의 JPA/QueryDSL 구현 어댑터.
 */
@Component
@RequiredArgsConstructor
public class TastePersistenceAdapter implements TasteRepositoryPort {

    private final TasteJpaRepository jpaRepository;
    private final TasteQueryRepository queryRepository;

    @Override
    public Optional<Taste> findById(Long tasteId) {
        return jpaRepository.findById(tasteId).map(TasteMapper::toDomain);
    }

    @Override
    public Optional<Taste> findByFilename(String filename) {
        return jpaRepository.findByFilename(filename).map(TasteMapper::toDomain);
    }

    @Override
    public List<Taste> findAll() {
        return jpaRepository.findAll().stream().map(TasteMapper::toDomain).toList();
    }

    @Override
    public List<Taste> findAllById(List<Long> tasteIds) {
        return jpaRepository.findAllById(tasteIds).stream().map(TasteMapper::toDomain).toList();
    }

    @Override
    public List<Taste> findTasteByCursor(Long cursorId, int size) {
        return queryRepository.findTasteByCursor(cursorId, size).stream().map(TasteMapper::toDomain).toList();
    }
}
