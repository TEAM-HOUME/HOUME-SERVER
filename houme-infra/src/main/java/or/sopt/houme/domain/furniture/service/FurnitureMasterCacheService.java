package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FurnitureMasterCacheService {

    private final FurnitureTypeRepository furnitureTypeRepository;
    private final FurnitureRepository furnitureRepository;

    @Cacheable(value = "allFurnitureTypesCache")
    public List<FurnitureType> getAllFurnitureTypes() {
        return furnitureTypeRepository.findAll();
    }

    @Cacheable(value = "allFurnituresCache")
    public List<FurnitureJpaEntity> getAllFurnitures() {
        return furnitureRepository.findAll();
    }
}
