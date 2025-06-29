package or.sopt.houme.domain.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KaKaoCallbackResponse {

    private String code;

    private String error;

    private String error_description;

    private String state;
}