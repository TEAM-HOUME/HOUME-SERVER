package or.sopt.houme.furniture.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.furniture.domain.FurnitureTagView;
import or.sopt.houme.furniture.domain.port.out.FurnitureTagQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** {@link FurnitureTagQueryPort} 의 JPA 구현 어댑터. 가구 요약 평탄화는 infra 내부 그래프 순회로 수행. */
@Component
@RequiredArgsConstructor
public class FurnitureTagQueryAdapter implements FurnitureTagQueryPort {

    private final FurnitureTagRepository furnitureTagRepository;

    static FurnitureTagView toView(FurnitureTag entity) {
        return new FurnitureTagView(
                entity.getId(),
                entity.getFurniturePrompt(),
                entity.getFurniture() != null ? entity.getFurniture().getId() : null,
                entity.getFurniture() != null ? entity.getFurniture().getFurnitureNameKr() : null,
                entity.getTagId(),
                entity.getFurnitureUrl(),
                entity.getSearchKeyword(),
                entity.getPriority()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FurnitureTagView> findByFurnitureIdAndTagId(Long furnitureId, Long tagId) {
        return furnitureTagRepository.findByFurnitureIdAndTagId(furnitureId, tagId)
                .map(FurnitureTagQueryAdapter::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FurnitureTagView> findAllByTagIdAndFurnitureIdIn(Long tagId, List<Long> furnitureIds) {
        if (furnitureIds == null || furnitureIds.isEmpty()) {
            return List.of();
        }
        return furnitureTagRepository.findAllByFurnitureIdInAndTagId(furnitureIds, tagId).stream()
                .map(FurnitureTagQueryAdapter::toView)
                .toList();
    }
}
