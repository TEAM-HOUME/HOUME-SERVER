package or.sopt.houme.domain.user.presentation.admin.controller.dto.tag;

public record AdminTagRequestDTO (

        int priority,
        String tagName,
        String tag_name_kr,
        String tag_prompt

){
}
