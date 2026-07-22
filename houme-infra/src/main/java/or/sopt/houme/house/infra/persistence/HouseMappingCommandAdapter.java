package or.sopt.houme.house.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFurniture;
import or.sopt.houme.domain.house.model.entity.mapping.HouseTaste;
import or.sopt.houme.domain.house.repository.HouseFurnitureRepository;
import or.sopt.houme.domain.house.repository.HouseTasteRepository;
import or.sopt.houme.furniture.domain.Furniture;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;
import or.sopt.houme.house.domain.port.out.HouseMappingCommandPort;
import or.sopt.houme.taste.infra.persistence.TasteJpaEntity;
import or.sopt.houme.taste.infra.persistence.TasteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link HouseMappingCommandPort} 의 JPA 구현 어댑터.
 * 존재하지 않는 id 를 조용히 건너뛰던 기존 동작을 보존하기 위해 실존 id 만 추려 저장한다.
 */
@Component
@RequiredArgsConstructor
public class HouseMappingCommandAdapter implements HouseMappingCommandPort {

    private final HouseFurnitureRepository houseFurnitureRepository;
    private final HouseTasteRepository houseTasteRepository;
    private final FurnitureRepositoryPort furnitureRepositoryPort;
    private final TasteJpaRepository tasteRepository;

    @Override
    public void saveHouseFurnitures(Long houseId, List<Long> furnitureIds) {
        if (furnitureIds == null || furnitureIds.isEmpty()) {
            return;
        }

        List<Furniture> furnitures = furnitureRepositoryPort.findAllById(furnitureIds);

        List<HouseFurniture> list = furnitures.stream()
                .map(furniture -> HouseFurniture.builder()
                        .houseId(houseId)
                        .furnitureId(furniture.getId())
                        .build())
                .toList();

        houseFurnitureRepository.saveAll(list);
    }

    @Override
    public void saveHouseTastes(Long houseId, List<Long> tasteIds) {
        List<TasteJpaEntity> tastes = tasteRepository.findAllById(tasteIds);

        List<HouseTaste> list = tastes.stream()
                .map(taste -> HouseTaste.builder()
                        .houseId(houseId)
                        .tasteId(taste.getId())
                        .build())
                .toList();

        houseTasteRepository.saveAll(list);
    }
}
