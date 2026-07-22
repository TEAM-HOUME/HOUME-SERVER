package or.sopt.houme.house.domain.port.out;

import or.sopt.houme.house.domain.House;

import java.util.Optional;

/**
 * 집 영속화 아웃바운드 포트 (#582 12b-2).
 */
public interface HouseRepositoryPort {

    Optional<House> findById(Long houseId);

    /** 유저의 가장 최근 등록 집. */
    Optional<House> findLatestByUserId(Long userId);

    /** 신규(id null)면 INSERT, 기존이면 변경 필드(activity/housePrompt) 반영. 저장 결과 반환. */
    House save(House house);
}
