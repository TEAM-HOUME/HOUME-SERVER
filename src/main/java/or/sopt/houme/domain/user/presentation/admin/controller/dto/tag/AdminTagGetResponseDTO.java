package or.sopt.houme.domain.user.presentation.admin.controller.dto.tag;

import or.sopt.houme.domain.house.model.taste.entity.Tag;

public record AdminTagGetResponseDTO(

        long id,
        int priority,
        String tagName,
        String tag_name_kr,
        String tag_prompt

){

    public static AdminTagGetResponseDTO of(Tag tag) {
        return new AdminTagGetResponseDTO(tag.getId(), tag.getPriority(), tag.getTagName(), tag.getTagNameKr(), tag.getTagPrompt());
    }

}
