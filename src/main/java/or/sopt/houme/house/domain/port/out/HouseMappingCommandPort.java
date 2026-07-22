package or.sopt.houme.house.domain.port.out;

import java.util.List;

/**
 * 집-가구/무드보드 매핑 저장 아웃바운드 포트.
 * 존재하지 않는 id 를 조용히 건너뛰는 기존 동작(실존 필터)은 구현이 보존한다.
 */
public interface HouseMappingCommandPort {

    void saveHouseFurnitures(Long houseId, List<Long> furnitureIds);

    void saveHouseTastes(Long houseId, List<Long> tasteIds);
}
