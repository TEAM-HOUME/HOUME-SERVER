package or.sopt.houme.domain.house.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.dto.HouseOptionDTO;
import or.sopt.houme.domain.house.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.InvalidHouseRequest;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.house.repository.InvalidHouseRequestRepository;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseServiceImpl implements HouseService {

    private final HouseRepository houseRepository;
    private final InvalidHouseRequestRepository invalidHouseRequestRepository;

    // 집구조 리스트 반환 서비스
    @Override
    public HouseOptionsResponse getHouseOptionsResponse() {
        // 주거 형태 리스트
        List<HouseOptionDTO> formList = Arrays.stream(Form.values())
                .map(e -> new HouseOptionDTO(e.name(), e.getDescription()))
                .toList();

        // 공간 구조 리스트
        List<HouseOptionDTO> structureList = Arrays.stream(Structure.values())
                .map(e -> new HouseOptionDTO(e.name(), e.getDescription()))
                .toList();

        // 평형 옵션 리스트
        List<HouseOptionDTO> equilibriumList = Arrays.stream(Equilibrium.values())
                .map(e -> new HouseOptionDTO(e.name(), e.getDescription()))
                .toList();

        // response로 반환
        return HouseOptionsResponse.of(formList, structureList, equilibriumList);
    }

    // 집 구조 선택 서비스
    @Transactional
    @Override
    public void selectHouseOptions(User user, HouseSelectRequest houseSelectRequest) {
        try {
            Form form = Form.valueOf(houseSelectRequest.housingType());
            Structure structure = Structure.valueOf(houseSelectRequest.roomType());
            Equilibrium equilibrium = Equilibrium.valueOf(houseSelectRequest.areaType());

            if (houseSelectRequest.isValid()){
                saveValidHouse(user, form, structure, equilibrium);
            } else {    // 유효하지 않은 요청일 시에 로그 남기기
                logInvalidHouseRequest(user, form, structure, equilibrium);
            }
        } catch (IllegalArgumentException e) {
            // 잘못된 enum값들 처리
            throw new GeneralException(ErrorCode.HOUSE_NOT_ALLOWED_OPTION);
        }
    }

    // 유효한 요청일 때 house 저장
    private void saveValidHouse(User user, Form form, Structure structure, Equilibrium equilibrium) {
        House house = House.builder()
                .form(form)
                .structure(structure)
                .equilibrium(equilibrium)
                .user(user)
                .build();
        houseRepository.save(house);
    }

    // 유효하지 않은 요청일 때 log 저장
    private void logInvalidHouseRequest(User user, Form form, Structure structure, Equilibrium equilibrium) {
        InvalidHouseRequest invalidRequest = InvalidHouseRequest.builder()
                .form(form)
                .structure(structure)
                .equilibrium(equilibrium)
                .user(user)
                .build();
        invalidHouseRequestRepository.save(invalidRequest);
    }
}
