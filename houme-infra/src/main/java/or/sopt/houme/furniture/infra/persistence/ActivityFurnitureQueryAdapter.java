package or.sopt.houme.furniture.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.repository.ActivityFurnitureRepository;
import or.sopt.houme.furniture.domain.ActivityFurnitureView;
import or.sopt.houme.furniture.domain.port.out.ActivityFurnitureQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** {@link ActivityFurnitureQueryPort} 의 JPA 구현 어댑터. 가구/타입 그래프 순회는 infra 내부에서 수행. */
@Component
@RequiredArgsConstructor
public class ActivityFurnitureQueryAdapter implements ActivityFurnitureQueryPort {

    private final ActivityFurnitureRepository activityFurnitureRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ActivityFurnitureView> findAllOrderByPriorityAscIdAsc() {
        return activityFurnitureRepository.findAllByOrderByPriorityAscIdAsc().stream()
                .map(mapping -> new ActivityFurnitureView(
                        mapping.getActivity(),
                        mapping.getPriority(),
                        FurniturePersistenceAdapter.toWithTypeView(mapping.getFurniture())
                ))
                .toList();
    }
}
