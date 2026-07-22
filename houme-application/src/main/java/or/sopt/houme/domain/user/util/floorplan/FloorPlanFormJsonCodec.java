package or.sopt.houme.domain.user.util.floorplan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FloorPlanFormJsonCodec {

    private static final TypeReference<List<Form>> FLOOR_PLAN_FORM_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public String write(List<Form> forms) {
        try {
            return objectMapper.writeValueAsString(forms);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    public List<Form> read(String formsJson) {
        if (formsJson == null || formsJson.isBlank()) {
            return List.of();
        }
        try {
            List<Form> forms = objectMapper.readValue(formsJson, FLOOR_PLAN_FORM_TYPE);
            if (forms == null) {
                return List.of();
            }
            return forms.stream().filter(Objects::nonNull).distinct().toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }
}
