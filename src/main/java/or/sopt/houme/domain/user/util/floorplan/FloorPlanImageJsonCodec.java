package or.sopt.houme.domain.user.util.floorplan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FloorPlanImageJsonCodec {

    private static final TypeReference<List<FloorPlanImageItem>> FLOOR_PLAN_IMAGE_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public String write(List<FloorPlanImageItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    public List<FloorPlanImageItem> read(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return List.of();
        }
        try {
            List<FloorPlanImageItem> items = objectMapper.readValue(imagesJson, FLOOR_PLAN_IMAGE_TYPE);
            if (items == null) {
                return List.of();
            }
            return items.stream()
                    .filter(Objects::nonNull)
                    .toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }
}
