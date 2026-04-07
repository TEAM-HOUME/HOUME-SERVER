package or.sopt.houme.domain.house.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.house.presentation.dto.HouseOptionDTO;
import or.sopt.houme.domain.house.presentation.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.presentation.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.presentation.dto.response.HouseIdResponse;
import or.sopt.houme.domain.house.presentation.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.InvalidHouseRequest;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFurniture;
import or.sopt.houme.domain.house.model.entity.mapping.HouseTaste;
import or.sopt.houme.domain.house.repository.*;
import or.sopt.houme.domain.house.model.taste.entity.Taste;
import or.sopt.houme.domain.house.repository.taste.taste.TasteRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.api.handler.HouseException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class HouseServiceImpl implements HouseService {

    private final HouseRepository houseRepository;
    private final InvalidHouseRequestRepository invalidHouseRequestRepository;
    private final HouseFloorPlanRepository houseFloorPlanRepository;
    private final FloorPlanRepository floorPlanRepository;
    private final HouseFurnitureRepository houseFurnitureRepository;
    private final FurnitureRepository furnitureRepository;
    private final TasteRepository tasteRepository;
    private final HouseTasteRepository houseTasteRepository;

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
                logInvalidHouseRequest(user, form, structure, equilibrium);
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
        House latestHouse = houseRepository.findLatestHouse(user);

        if (latestHouse == null) {
            throw new GeneralException(ErrorCode.NOT_FOUND_HOUSE);
        }
        FloorPlan floorPlan = getFloorPlanOrThrow(latestHouse);
        return new LatestHouseConditionDTO(floorPlan.getForm(), floorPlan.getStructure(), floorPlan.getEquilibrium());
    }

    // house prompt 저장
    @Transactional
    @Override
    public void saveHousePrompt(House house, String prompt) {
        house.updatePrompt(prompt);

        houseRepository.save(house);
    }

    @Transactional
    @Override
    public House createTemplateHouse(User user, Banner banner, String prompt, Long floorPlanId, boolean isMirror) {
        FloorPlan floorPlan = floorPlanRepository.findById(floorPlanId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

        House house = House.builder()
                .activity(null)
                .user(user)
                .banner(banner)
                .isValid(true)
                .housePrompt(prompt)
                .build();

        House savedHouse = houseRepository.save(house);
        saveHouseFloorPlan(savedHouse, floorPlanId, isMirror);
        return savedHouse;
    }

    // house activity 업데이트
    @Transactional
    @Override
    public House updateHouseActivity(Long houseId, Activity activity) {

        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE));

        house.updateActivity(activity);
        return houseRepository.save(house);
    }

    // 집 도면 매핑 테이블 저장
    @Transactional
    @Override
    public void saveHouseFloorPlan(House house, Long floorPlanId, boolean isMirror) {

        FloorPlan floorPlan = floorPlanRepository.findById(floorPlanId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

        HouseFloorPlan houseFloorPlan = HouseFloorPlan.builder()
                .house(house)
                .floorPlan(floorPlan)
                .isReverse(isMirror)
                .build();

        houseFloorPlanRepository.save(houseFloorPlan);
    }

    @Override
    public House findHouseById(long houseId) {
        return houseRepository.findById(houseId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_HOUSE));
    }

    // house와 furniture 저장
    @Transactional
    @Override
    public void saveHouseFurniture(House house, List<Long> furnitureIds) {

        List<Furniture> furnitures = furnitureRepository.findAllById(furnitureIds);

        List<HouseFurniture> list = furnitures.stream()
                .map(furniture -> HouseFurniture.builder()
                        .house(house)
                        .furniture(furniture)
                        .build())
                .toList();

        houseFurnitureRepository.saveAll(list);
    }

    // house와 무드보드(taste) 저장
    @Transactional
    @Override
    public void saveHouseTaste(House house, List<Long> tasteIds) {

        List<Taste> tastes = tasteRepository.findAllById(tasteIds);

        List<HouseTaste> list = tastes.stream()
                .map(taste -> HouseTaste.builder()
                        .house(house)
                        .taste(taste)
                        .build())
                .toList();

        houseTasteRepository.saveAll(list);
    }

    @Override
    public boolean getIsMirrorByHouseId(Long houseId) {
        HouseFloorPlan houseFloorPlan = houseFloorPlanRepository.findHouseFloorPlanByHouseId(houseId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

        return houseFloorPlan.isReverse();
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

    // 유효한 요청일 때 house 저장
    private Long saveValidHouse(User user, Form form, Structure structure, Equilibrium equilibrium) {
        FloorPlan matchedFloorPlan = floorPlanRepository.findFirstByFormAndStructureAndEquilibrium(form, structure, equilibrium)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

        House house = House.builder()
                .user(user)
                .isValid(true)
                .build();
        House save = houseRepository.save(house);
        saveHouseFloorPlan(save, matchedFloorPlan.getId(), false);

        return save.getId();
    }

    private FloorPlan getFloorPlanOrThrow(House house) {
        return houseFloorPlanRepository.findHouseFloorPlanByHouseId(house.getId())
                .map(HouseFloorPlan::getFloorPlan)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));
    }
}
