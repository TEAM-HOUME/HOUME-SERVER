package or.sopt.houme.furniture.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.furniture.domain.FurnitureTypeView;
import or.sopt.houme.furniture.domain.port.out.FurnitureTypeQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;

/** {@link FurnitureTypeQueryPort} 의 JPA 구현 어댑터. */
@Component
@RequiredArgsConstructor
public class FurnitureTypeQueryAdapter implements FurnitureTypeQueryPort {

    private final FurnitureTypeRepository furnitureTypeRepository;

    static FurnitureTypeView toView(FurnitureType entity) {
        return new FurnitureTypeView(entity.getId(), entity.getNameKr(), entity.getNameEng(),
                entity.getIsRequired(), entity.getPriority());
    }

    @Override
    public List<FurnitureTypeView> findAll() {
        return furnitureTypeRepository.findAll().stream()
                .map(FurnitureTypeQueryAdapter::toView)
                .toList();
    }
}
