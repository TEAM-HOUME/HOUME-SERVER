package or.sopt.houme.domain.admin.controller.dto;

public record AdminTagUpdateRequestDTO(

        String tagNameKr,
        Integer newPriority,
        String newTagNameEng,
        String newTagPrompt


) {
}
