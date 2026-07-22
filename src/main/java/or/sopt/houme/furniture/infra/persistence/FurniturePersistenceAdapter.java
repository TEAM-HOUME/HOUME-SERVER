package or.sopt.houme.furniture.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.furniture.domain.Furniture;
import or.sopt.houme.furniture.domain.FurnitureWithTypeView;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link FurnitureRepositoryPort} 의 JPA 구현 어댑터.
 *
 * <p>기존 {@link FurnitureRepository}(JpaRepo)를 재사용하고, 경계를 넘어 반환할 때만
 * 순수 도메인/뷰로 변환한다. 뷰 조합을 위한 그래프 순회(fetch join)는 infra 내부에서 자유롭게 한다.
 */
@Component
@RequiredArgsConstructor
public class FurniturePersistenceAdapter implements FurnitureRepositoryPort {

    private final FurnitureRepository furnitureRepository;
    private final JPAQueryFactory queryFactory;

    static FurnitureWithTypeView toWithTypeView(FurnitureJpaEntity entity) {
        var type = entity.getFurnitureType();
        return new FurnitureWithTypeView(
                entity.getId(),
                entity.getFurnitureNameEng(),
                entity.getFurnitureNameKr(),
                entity.getPriority(),
                type != null ? type.getId() : null,
                type != null ? type.getNameKr() : null,
                type != null ? type.getNameEng() : null
        );
    }

    @Override
    public Optional<Furniture> findById(Long id) {
        return furnitureRepository.findById(id).map(FurnitureMapper::toDomain);
    }

    @Override
    public List<Furniture> findAllById(List<Long> ids) {
        return furnitureRepository.findAllById(ids).stream()
                .map(FurnitureMapper::toDomain)
                .toList();
    }

    @Override
    public List<Furniture> findAllByHouseId(Long houseId) {
        return furnitureRepository.findAllByHouseId(houseId).stream()
                .map(FurnitureMapper::toDomain)
                .toList();
    }

    @Override
    public List<FurnitureWithTypeView> findAllWithType() {
        return furnitureRepository.findAllWithFurnitureType().stream()
                .map(FurniturePersistenceAdapter::toWithTypeView)
                .toList();
    }

    @Override
    public List<FurnitureWithTypeView> findAllWithTypeByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        QFurnitureJpaEntity furniture = QFurnitureJpaEntity.furnitureJpaEntity;
        return queryFactory
                .selectFrom(furniture)
                .join(furniture.furnitureType).fetchJoin()
                .where(furniture.id.in(ids))
                .fetch()
                .stream()
                .map(FurniturePersistenceAdapter::toWithTypeView)
                .toList();
    }
}
