package or.sopt.houme.domain.house.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.dto.HouseOptionDTO;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.entity.Equilibrium;
import or.sopt.houme.domain.house.entity.Form;
import or.sopt.houme.domain.house.entity.Structure;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseServiceImpl implements HouseService {

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

}
