package or.sopt.houme.house.domain.port.out;

import java.util.Optional;

/** 집 조회 아웃바운드 포트 (도메인 경계를 넘는 최소 조회만 노출). */
public interface HouseQueryPort {

    Optional<Long> findHouseIdByUserIdAndImageId(Long userId, Long imageId);
}
