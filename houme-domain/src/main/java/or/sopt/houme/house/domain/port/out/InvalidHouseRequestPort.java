package or.sopt.houme.house.domain.port.out;

import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

/**
 * 유효하지 않은 집 요청 로그 아웃바운드 포트.
 */
public interface InvalidHouseRequestPort {

    void log(Long userId, Form form, Structure structure, Equilibrium equilibrium);
}
