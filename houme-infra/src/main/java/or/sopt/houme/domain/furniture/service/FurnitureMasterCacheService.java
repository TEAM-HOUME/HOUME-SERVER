package or.sopt.houme.domain.furniture.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 가구/가구타입 마스터 데이터 캐시.
 *
 * 엔티티를 그대로 캐시에 넣지 않고 읽기 모델(*CacheView)로 변환해 저장한다.
 * 엔티티를 넣으면 가구 ↔ 가구태그 순환 참조로 Redis JSON 직렬화가 무한 재귀에 빠지고(#618),
 * 캐시 값도 사용하지 않는 필드·지연 로딩 컬렉션까지 끌고 들어가 불필요하게 커진다.
 *
 * 반환 리스트는 ArrayList여야 한다 — Stream.toList()의 ImmutableCollections$ListN은
 * GenericJackson2JsonRedisSerializer가 역직렬화(캐시 히트)할 때 인스턴스화하지 못한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FurnitureMasterCacheService {

    private final FurnitureTypeRepository furnitureTypeRepository;
    private final FurnitureRepository furnitureRepository;

    @Cacheable(value = "allFurnitureTypesCache")
    public List<FurnitureTypeCacheView> getAllFurnitureTypes() {
        return furnitureTypeRepository.findAll().stream()
                .map(type -> new FurnitureTypeCacheView(type.getId(), type.getNameEng()))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "allFurnituresCache")
    public List<FurnitureCacheView> getAllFurnitures() {
        return furnitureRepository.findAll().stream()
                .map(furniture -> new FurnitureCacheView(furniture.getId(), furniture.getFurnitureNameEng()))
                .collect(Collectors.toList());
    }
}
