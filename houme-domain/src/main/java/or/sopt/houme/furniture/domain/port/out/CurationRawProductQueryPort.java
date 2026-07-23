package or.sopt.houme.furniture.domain.port.out;

import or.sopt.houme.furniture.domain.CurationRawProductColorView;
import or.sopt.houme.furniture.domain.CurationRawProductView;

import java.util.List;
import java.util.Optional;

/**
 * 큐레이션 원본상품 조회 아웃바운드 포트 (read model 반환).
 * 쓰기/관리자 경로는 curation infra 가 엔티티로 직접 다루고, 경계를 넘는 읽기만 이 포트로 나간다.
 */
public interface CurationRawProductQueryPort {

    Optional<CurationRawProductView> findById(Long id);

    List<CurationRawProductView> findAllByProductIdIn(List<Long> productIds);

    List<CurationRawProductColorView> findColorsByRawProductIdIn(List<Long> rawProductIds);
}
