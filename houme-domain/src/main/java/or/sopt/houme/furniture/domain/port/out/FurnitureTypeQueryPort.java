package or.sopt.houme.furniture.domain.port.out;

import or.sopt.houme.furniture.domain.FurnitureTypeView;

import java.util.List;

/** 가구 타입 조회 아웃바운드 포트. */
public interface FurnitureTypeQueryPort {

    List<FurnitureTypeView> findAll();
}
