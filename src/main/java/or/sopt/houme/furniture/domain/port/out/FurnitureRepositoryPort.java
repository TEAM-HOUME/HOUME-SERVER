package or.sopt.houme.furniture.domain.port.out;

import or.sopt.houme.furniture.domain.Furniture;

import java.util.List;

/**
 * 가구 영속화 아웃바운드 포트. 도메인 경계를 넘는 소비처(House/User 등)는 이 인터페이스만 알고,
 * 구현(JPA)은 infra 어댑터가 제공한다.
 *
 * <p>furniture 도메인 내부 서비스는 여전히 {@code FurnitureRepository}(JPA)를 직접 사용하므로,
 * 이 포트는 외부 소비처가 필요로 하는 최소 조회만 노출한다.
 */
public interface FurnitureRepositoryPort {

    /** id 목록으로 실존 가구만 조회 (존재하지 않는 id 는 결과에서 제외). */
    List<Furniture> findAllById(List<Long> ids);
}
