package or.sopt.houme.furniture.domain.port.out;

import or.sopt.houme.furniture.domain.Furniture;
import or.sopt.houme.furniture.domain.FurnitureWithTypeView;

import java.util.List;
import java.util.Optional;

/**
 * 가구 영속화 아웃바운드 포트. 도메인 경계를 넘는 소비처(House/User 등)는 이 인터페이스만 알고,
 * 구현(JPA)은 infra 어댑터가 제공한다.
 */
public interface FurnitureRepositoryPort {

    Optional<Furniture> findById(Long id);

    /** id 목록으로 실존 가구만 조회 (존재하지 않는 id 는 결과에서 제외). */
    List<Furniture> findAllById(List<Long> ids);

    /** 특정 집(house)에 매핑된 가구들 조회. */
    List<Furniture> findAllByHouseId(Long houseId);

    /** 전체 가구를 타입 정보와 함께 조회 (대시보드용). */
    List<FurnitureWithTypeView> findAllWithType();

    /** id 목록의 가구를 타입 정보와 함께 조회. */
    List<FurnitureWithTypeView> findAllWithTypeByIdIn(List<Long> ids);
}
