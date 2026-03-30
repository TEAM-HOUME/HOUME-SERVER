package or.sopt.houme.domain.user.util.floorplan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FloorPlanStructureJsonCodec {

    private static final TypeReference<List<Structure>> FLOOR_PLAN_STRUCTURE_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public String write(List<Structure> structures) {
        try {
            return objectMapper.writeValueAsString(structures);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    public List<Structure> read(String structuresJson) {
        if (structuresJson == null || structuresJson.isBlank()) {
            return List.of();
        }
        try {
            List<Structure> structures = objectMapper.readValue(structuresJson, FLOOR_PLAN_STRUCTURE_TYPE);
            if (structures == null) {
                return List.of();
            }
            return structures.stream().filter(Objects::nonNull).distinct().toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }
}
