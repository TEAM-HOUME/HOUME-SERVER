package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.AdminTagUpdateRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagDeleteRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagGetAllResponseDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagRequestDTO;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.tag.AdminTagGetResponseDTO;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private final TagRepository tagRepository;


    /**
     * 태그 엔티티를 생성하는 메서드입니다.
     * 어드민 사용의 편리함과 기획의도를 고려하여 Tag의 한글명을 unique 필드로 사용중입니다.
     *
     * @throws GeneralException 로직 중 무결성 제약조건에 부합하지 않으면 예외 발생
     * @throws GeneralException 이미 존재하는 우선순위가 있다면 예외 발생
     * @throws DataIntegrityViolationException 데이터 저장 중 무결성 제약조건에 부합하지 않으면 예외 발생
     * */
    @Override
    public void create(AdminTagRequestDTO dto){

        Optional<Tag> byTagNameKr = tagRepository.findByTagNameKr(dto.tag_name_kr());

        if(byTagNameKr.isPresent()){
            throw new GeneralException(ErrorCode.ALREADY_EXIST_TAG);
        }

        Optional<Tag> byPriority = tagRepository.findByPriority(dto.priority());

        if (byPriority.isPresent()){
            throw new GeneralException(ErrorCode.ALREADY_EXIST_PRIORITY);
        }

        Tag newTag = Tag.of(dto.tagName(), dto.priority(), dto.tag_name_kr(), dto.tag_prompt());

        try {
            tagRepository.save(newTag);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.ALREADY_EXIST_TAG);
        }
    }


    /**
     * 현재 존재하는 태그를 모두 조회합니다
     * 조회 시, 눈에 잘 보일 수 있도록 우선순위를 기준으로 정렬하여 조회합니다
     * */
    @Override
    public AdminTagGetAllResponseDTO getAll() {

        List<Tag> tags = tagRepository.findAllByOrderByPriorityAsc();

        List<AdminTagGetResponseDTO> responseDTOS = tags.stream()
                .map(AdminTagGetResponseDTO::of).toList();

        return new AdminTagGetAllResponseDTO(responseDTOS);
    }


    /**
     * 태그 정보를 업데이트합니다
     *
     * 1. 태그의 영어명
     * 2. 우선순위
     * 3. 프롬프트
     *
     * 를 수정 할 수 있습니다.
     *
     * @throws GeneralException 로직 중 우선순위가 이미 있는 데이터로 들어오면 예외 발생
     * */
    @Override
    public void update(AdminTagUpdateRequestDTO dto){

        log.info("------------업데이트를 시작합니다------------");

        Tag tag = tagRepository.findById(dto.tagId())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        log.info("------------태그 ID를 찾았습니다------------");

        log.info("------------우선순위를 업데이트 합니다------------");
        if (dto.newPriority() != null) {
            Optional<Tag> byPriority = tagRepository.findByPriority(dto.newPriority());
            if (byPriority.isPresent() && (byPriority.get().getId() == null || !byPriority.get().getId().equals(tag.getId()))) {
                throw new GeneralException(ErrorCode.ALREADY_EXIST_PRIORITY);
            }
        }

        if (dto.newTagNameKr() != null && !dto.newTagNameKr().isBlank()) {
            Optional<Tag> byTagNameKr = tagRepository.findByTagNameKr(dto.newTagNameKr());
            if (byTagNameKr.isPresent() && (byTagNameKr.get().getId() == null || !byTagNameKr.get().getId().equals(tag.getId()))) {
                throw new GeneralException(ErrorCode.ALREADY_EXIST_TAG);
            }
        }

        tag.update(dto);
    }


    /**
     * 태그 데이터를 삭제하는 메서드입니다.
     *
     * @throws GeneralException 태그 정보를 찾을 수 없을 때
     * @throws DataIntegrityViolationException 연관된 데이터가 존재하는 경우 예외 발생
     * */
    @Override
    public void delete(AdminTagDeleteRequestDTO dto){
        Tag tag = tagRepository.findById(dto.tagId())
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        try {
            tagRepository.delete(tag);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorCode.FOREIGN_KEY_CONSTRAINT_FAIL);
        }
    }
}
