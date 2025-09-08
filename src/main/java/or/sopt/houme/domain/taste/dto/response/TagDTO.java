package or.sopt.houme.domain.taste.dto.response;

import or.sopt.houme.domain.taste.entity.Tag;

public record TagDTO(
        Long id,
        String tagName,
        int priority,
        String tagNameKr,
        String tagPrompt
) {
    public static TagDTO of(Tag tag){
        return  new TagDTO(
                tag.getId(), tag.getTagName(),  tag.getPriority(), tag.getTagNameKr(), tag.getTagPrompt()
        );
    }
}
