package or.sopt.houme.furniture.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.furniture.domain.Furniture;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link FurnitureRepositoryPort} 의 JPA 구현 어댑터.
 *
 * <p>furniture 도메인 내부에서 이미 사용 중인 {@link FurnitureRepository}(JpaRepository)를 재사용하고,
 * 경계를 넘어 반환할 때만 {@link FurnitureMapper} 로 순수 도메인 모델로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class FurniturePersistenceAdapter implements FurnitureRepositoryPort {

    private final FurnitureRepository furnitureRepository;

    @Override
    public List<Furniture> findAllById(List<Long> ids) {
        return furnitureRepository.findAllById(ids).stream()
                .map(FurnitureMapper::toDomain)
                .toList();
    }
}
