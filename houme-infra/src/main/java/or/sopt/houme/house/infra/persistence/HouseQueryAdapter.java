package or.sopt.houme.house.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.house.domain.port.out.HouseQueryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** {@link HouseQueryPort} 의 JPA 구현 어댑터. */
@Component
@RequiredArgsConstructor
public class HouseQueryAdapter implements HouseQueryPort {

    private final HouseRepository houseRepository;

    @Override
    public Optional<Long> findHouseIdByUserIdAndImageId(Long userId, Long imageId) {
        return houseRepository.findHouseByUserIdAndImageId(userId, imageId).map(HouseJpaEntity::getId);
    }
}
