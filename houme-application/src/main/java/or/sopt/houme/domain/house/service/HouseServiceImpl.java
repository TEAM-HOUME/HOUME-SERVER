package or.sopt.houme.domain.house.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.house.presentation.dto.HouseOptionDTO;
import or.sopt.houme.domain.house.presentation.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.presentation.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.presentation.dto.response.HouseIdResponse;
import or.sopt.houme.domain.house.presentation.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.house.domain.FloorPlanCondition;
import or.sopt.houme.house.domain.House;
import or.sopt.houme.house.domain.port.out.FloorPlanQueryPort;
import or.sopt.houme.house.domain.port.out.HouseFloorPlanPort;
import or.sopt.houme.house.domain.port.out.HouseMappingCommandPort;
import or.sopt.houme.house.domain.port.out.HouseRepositoryPort;
import or.sopt.houme.house.domain.port.out.InvalidHouseRequestPort;
import or.sopt.houme.user.domain.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.HouseException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * #582 12b-2: 순수 House 도메인 + 포트만 소비하는 애플리케이션 서비스 (JPA 참조 0).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class HouseServiceImpl implements HouseService {

    private final HouseRepositoryPort houseRepositoryPort;
    private final HouseFloorPlanPort houseFloorPlanPort;
    private final HouseMappingCommandPort houseMappingCommandPort;
    private final FloorPlanQueryPort floorPlanQueryPort;
    private final InvalidHouseRequestPort invalidHouseRequestPort;

    // 집구조 리스트 반환 서비스
    @Cacheable(value = "houseOptionsCache")
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
    public HouseIdResponse selectHouseOptions(User user, HouseSelectRequest houseSelectRequest) {
        try {
            Form form = Form.valueOf(houseSelectRequest.houseType());
            Structure structure = Structure.valueOf(houseSelectRequest.roomType());
            Equilibrium equilibrium = Equilibrium.valueOf(houseSelectRequest.areaType());

            if (houseSelectRequest.isValid()){
                 return HouseIdResponse.of(saveValidHouse(user, form, structure, equilibrium));
            } else {    // 유효하지 않은 요청일 시에 로그 남기기
                invalidHouseRequestPort.log(user.getId(), form, structure, equilibrium);
                return null;
            }
        } catch (IllegalArgumentException e) {
            // 잘못된 enum값들 처리
            throw new GeneralException(ErrorCode.HOUSE_NOT_ALLOWED_OPTION);
        }
    }

    // 가장 최근 등록한 House 찾기
    @Override
    public LatestHouseConditionDTO findLatestHouse(User user) {
        House latestHouse = houseRepositoryPort.findLatestByUserId(user.getId())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_HOUSE));

        FloorPlanCondition condition = houseFloorPlanPort.findConditionByHouseId(latestHouse.getId())
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));
        return new LatestHouseConditionDTO(condition.form(), condition.structure(), condition.equilibrium());
    }

    // house prompt 저장
    @Transactional
    @Override
    public void saveHousePrompt(Long houseId, String prompt) {
        House house = houseRepositoryPort.findById(houseId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE));
        house.updatePrompt(prompt);

        houseRepositoryPort.save(house);
    }

    @Transactional
    @Override
    public House createTemplateHouse(User user, Long bannerId, String prompt, Long floorPlanId, boolean isMirror) {
        return createTemplateHouse(user, bannerId, prompt, floorPlanId, isMirror, null);
    }

    @Transactional
    @Override
    public House createTemplateHouse(User user, Long bannerId, String prompt, Long floorPlanId, boolean isMirror, String selectedView) {
        if (!floorPlanQueryPort.existsById(floorPlanId)) {
            throw new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN);
        }

        House savedHouse = houseRepositoryPort.save(House.create(null, user.getId(), bannerId, true, prompt));
        saveHouseFloorPlan(savedHouse.getId(), floorPlanId, isMirror, selectedView);
        return savedHouse;
    }

    // house activity 업데이트
    @Transactional
    @Override
    public House updateHouseActivity(Long houseId, Activity activity) {

        House house = houseRepositoryPort.findById(houseId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE));

        house.updateActivity(activity);
        return houseRepositoryPort.save(house);
    }

    // 집 도면 매핑 테이블 저장
    @Transactional
    @Override
    public void saveHouseFloorPlan(Long houseId, Long floorPlanId, boolean isMirror) {
        saveHouseFloorPlan(houseId, floorPlanId, isMirror, null);
    }

    @Transactional
    @Override
    public void saveHouseFloorPlan(Long houseId, Long floorPlanId, boolean isMirror, String selectedView) {
        houseFloorPlanPort.save(houseId, floorPlanId, isMirror, selectedView);
    }

    @Override
    public House findHouseById(long houseId) {
        return houseRepositoryPort.findById(houseId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE));
    }

    // house와 furniture 저장
    @Transactional
    @Override
    public void saveHouseFurniture(Long houseId, List<Long> furnitureIds) {
        houseMappingCommandPort.saveHouseFurnitures(houseId, furnitureIds);
    }

    // house와 무드보드(taste) 저장
    @Transactional
    @Override
    public void saveHouseTaste(Long houseId, List<Long> tasteIds) {
        houseMappingCommandPort.saveHouseTastes(houseId, tasteIds);
    }

    @Override
    public boolean getIsMirrorByHouseId(Long houseId) {
        return houseFloorPlanPort.findIsMirrorByHouseId(houseId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));
    }

    // 유효한 요청일 때 house 저장
    private Long saveValidHouse(User user, Form form, Structure structure, Equilibrium equilibrium) {
        Long matchedFloorPlanId = floorPlanQueryPort.findFirstIdByCondition(form, structure, equilibrium)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

        House save = houseRepositoryPort.save(House.create(null, user.getId(), null, true, null));
        saveHouseFloorPlan(save.getId(), matchedFloorPlanId, false);

        return save.getId();
    }
}
