package or.sopt.houme.domain.admin.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.AdminTagUpdateRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.tag.AdminTagGetAllResponseDTO;
import or.sopt.houme.domain.admin.controller.dto.tag.AdminTagRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.tag.AdminTagGetResponseDTO;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private final TagRepository tagRepository;


    @Override
    public void create(AdminTagRequestDTO dto){

        Optional<Tag> byTagNameKr = tagRepository.findByTagNameKr(dto.tag_name_kr());

        if(byTagNameKr.isPresent()){
            throw new GeneralException(ErrorCode.ALREADY_EXIST_TAG);
        }

        Tag newTag = Tag.of(dto.tagName(), dto.priority(), dto.tag_name_kr(), dto.tag_prompt());
        tagRepository.save(newTag);
    }


    @Override
    public AdminTagGetAllResponseDTO getAll() {

        List<Tag> tags = tagRepository.findAll();

        List<AdminTagGetResponseDTO> responseDTOS = tags.stream()
                .map(tag -> new AdminTagGetResponseDTO(
                        tag.getPriority(),
                        tag.getTagName(),
                        tag.getTagNameKr(),
                        tag.getTagPrompt()))
                .toList();

        return new AdminTagGetAllResponseDTO(responseDTOS);
    }


    @Override
    public void update(AdminTagUpdateRequestDTO dto){

        Tag byTagNameKr = tagRepository.findByTagNameKr(dto.tagNameKr())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        byTagNameKr.update(dto);
    }
}
