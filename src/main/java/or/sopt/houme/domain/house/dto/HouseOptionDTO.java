package or.sopt.houme.domain.house.dto;

public record HouseOptionDTO(
        String code,
        String description
) {
    // Enum을 DTO로 변환
    public static HouseOptionDTO fromEnum(Enum<?> e, String description) {
        return new HouseOptionDTO(e.name(), description);
    }
}
