package or.sopt.houme.house.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.entity.InvalidHouseRequest;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.repository.InvalidHouseRequestRepository;
import or.sopt.houme.house.domain.port.out.InvalidHouseRequestPort;
import org.springframework.stereotype.Component;

/** {@link InvalidHouseRequestPort} 의 JPA 구현 어댑터. */
@Component
@RequiredArgsConstructor
public class InvalidHouseRequestAdapter implements InvalidHouseRequestPort {

    private final InvalidHouseRequestRepository invalidHouseRequestRepository;

    @Override
    public void log(Long userId, Form form, Structure structure, Equilibrium equilibrium) {
        InvalidHouseRequest invalidRequest = InvalidHouseRequest.builder()
                .form(form)
                .structure(structure)
                .equilibrium(equilibrium)
                .userId(userId)
                .build();
        invalidHouseRequestRepository.save(invalidRequest);
    }
}
