package or.sopt.houme.domain.user.util.floorplan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FloorPlanEquilibriumJsonCodec {

    private static final TypeReference<List<Equilibrium>> FLOOR_PLAN_EQUILIBRIUM_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public String write(List<Equilibrium> equilibriums) {
        try {
            return objectMapper.writeValueAsString(equilibriums);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    public List<Equilibrium> read(String equilibriumsJson) {
        if (equilibriumsJson == null || equilibriumsJson.isBlank()) {
            return List.of();
        }
        try {
            List<Equilibrium> equilibriums = objectMapper.readValue(equilibriumsJson, FLOOR_PLAN_EQUILIBRIUM_TYPE);
            if (equilibriums == null) {
                return List.of();
            }
            return equilibriums.stream().filter(Objects::nonNull).distinct().toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }
}
