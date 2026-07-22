package or.sopt.houme.furniture.domain.port.out;

import or.sopt.houme.furniture.domain.ActivityFurnitureView;

import java.util.List;

/** 주요활동-가구 매핑 조회 아웃바운드 포트. */
public interface ActivityFurnitureQueryPort {

    /** 우선순위 ASC, id ASC 정렬. */
    List<ActivityFurnitureView> findAllOrderByPriorityAscIdAsc();
}
