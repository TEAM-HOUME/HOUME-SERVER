package or.sopt.houme.house.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.house.domain.House;
import or.sopt.houme.house.domain.port.out.HouseRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link HouseRepositoryPort} 의 JPA 구현 어댑터 (#582 12b-2).
 * HouseJpaEntity 의 banner @ManyToOne(infra 내부 연관)은 bannerId 로 관리참조 해소한다.
 */
@Component
@RequiredArgsConstructor
public class HousePersistenceAdapter implements HouseRepositoryPort {

    private final HouseRepository houseRepository;
    private final BannerRepository bannerRepository;

    @Override
    public Optional<House> findById(Long houseId) {
        return houseRepository.findById(houseId).map(HouseMapper::toDomain);
    }

    @Override
    public Optional<House> findLatestByUserId(Long userId) {
        return Optional.ofNullable(houseRepository.findLatestHouse(userId)).map(HouseMapper::toDomain);
    }

    @Override
    public House save(House house) {
        if (house.getId() == null) {
            HouseJpaEntity entity = HouseJpaEntity.builder()
                    .activity(house.getActivity())
                    .userId(house.getUserId())
                    .banner(house.getBannerId() != null ? bannerRepository.getReferenceById(house.getBannerId()) : null)
                    .isValid(house.isValid())
                    .housePrompt(house.getHousePrompt())
                    .build();
            return HouseMapper.toDomain(houseRepository.save(entity));
        }
        // 기존 집: 조회 후 변경 필드만 반영 (트랜잭션 내 더티 체킹으로 UPDATE). 못 찾으면 fail-fast.
        HouseJpaEntity entity = houseRepository.findById(house.getId())
                .orElseThrow(() -> new IllegalStateException("상태를 갱신할 집을 찾을 수 없습니다. id=" + house.getId()));
        entity.updateActivity(house.getActivity());
        entity.updatePrompt(house.getHousePrompt());
        return HouseMapper.toDomain(houseRepository.save(entity));
    }
}
