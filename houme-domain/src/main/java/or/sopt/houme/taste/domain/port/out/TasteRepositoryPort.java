package or.sopt.houme.taste.domain.port.out;

import or.sopt.houme.taste.domain.Taste;

import java.util.List;
import java.util.Optional;

/**
 * 취향(무드보드) 영속화 아웃바운드 포트. 도메인/애플리케이션은 이 인터페이스만 알고,
 * 구현(JPA·QueryDSL)은 infra 어댑터가 제공한다.
 */
public interface TasteRepositoryPort {

    Optional<Taste> findById(Long tasteId);

    Optional<Taste> findByFilename(String filename);

    List<Taste> findAll();

    List<Taste> findAllById(List<Long> tasteIds);

    /** cursor 기반 페이지네이션. */
    List<Taste> findTasteByCursor(Long cursorId, int size);
}
