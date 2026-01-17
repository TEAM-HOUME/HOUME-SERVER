package or.sopt.houme.domain.house.presentation.dto.response;

public record HouseIdResponse(
        Long houseId
) {
    public static   HouseIdResponse of(Long houseId) {
        return new HouseIdResponse(houseId);
    }
}
