package or.sopt.houme.domain.house.dto.response;

import or.sopt.houme.domain.house.dto.HouseOptionDTO;

import java.util.List;

public record HouseOptionsResponse(
        List<HouseOptionDTO> houseTypes,  // 주거형태
        List<HouseOptionDTO> roomTypes,     // 공간구조
        List<HouseOptionDTO> areaTypes      // 평형
) {
    public static HouseOptionsResponse of(
            List<HouseOptionDTO> housingTypes,
            List<HouseOptionDTO> roomTypes,
            List<HouseOptionDTO> areaTypes
    ) {
        return new HouseOptionsResponse(housingTypes, roomTypes, areaTypes);
    }
}
