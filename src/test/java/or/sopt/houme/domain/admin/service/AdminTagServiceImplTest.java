package or.sopt.houme.domain.admin.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import or.sopt.houme.domain.admin.controller.dto.AdminTagUpdateRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.tag.AdminTagDeleteRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.tag.AdminTagGetAllResponseDTO;
import or.sopt.houme.domain.admin.controller.dto.tag.AdminTagGetResponseDTO;
import or.sopt.houme.domain.admin.controller.dto.tag.AdminTagRequestDTO;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminTagServiceImplTest {



    @InjectMocks
    private AdminTagServiceImpl adminTagService;

    @Mock
    private TagRepository tagRepository;



    @Test
    @DisplayName("create()는 태그를 성공적으로 생성한다")
    void create_success() {
        // given
        AdminTagRequestDTO requestDTO = new AdminTagRequestDTO(1, "minimal", "미니멀", "a minimal mood");
        when(tagRepository.findByTagNameKr("미니멀")).thenReturn(Optional.empty());
        when(tagRepository.findByPriority(1)).thenReturn(Optional.empty());

        // when
        adminTagService.create(requestDTO);

        // then
        verify(tagRepository, times(1)).save(any(Tag.class));
    }


    @Test
    @DisplayName("create()는 이미 존재하는 태그 이름으로 생성 시 예외를 발생시킨다")
    void create_alreadyExistTag() {
        // given
        AdminTagRequestDTO requestDTO = new AdminTagRequestDTO(1, "minimal", "미니멀", "a minimal mood");
        when(tagRepository.findByTagNameKr("미니멀")).thenReturn(Optional.of(Tag.builder().build()));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminTagService.create(requestDTO);
        });
        assertEquals(ErrorCode.ALREADY_EXIST_TAG, exception.getErrorCode());
    }


    @Test
    @DisplayName("create()는 이미 존재하는 우선순위로 생성 시 예외를 발생시킨다")
    void create_alreadyExistPriority() {
        // given
        AdminTagRequestDTO requestDTO = new AdminTagRequestDTO(1, "minimal", "미니멀", "a minimal mood");
        when(tagRepository.findByTagNameKr("미니멀")).thenReturn(Optional.empty());
        when(tagRepository.findByPriority(1)).thenReturn(Optional.of(Tag.builder().build()));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminTagService.create(requestDTO);
        });
        assertEquals(ErrorCode.ALREADY_EXIST_PRIORITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("create()는 데이터 저장 중 무결성 제약 조건 위반 시 예외를 발생시킨다")
    void create_dataIntegrityViolation() {
        // given
        AdminTagRequestDTO requestDTO = new AdminTagRequestDTO(1, "minimal", "미니멀", "a minimal mood");
        when(tagRepository.findByTagNameKr("미니멀")).thenReturn(Optional.empty());
        when(tagRepository.findByPriority(1)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenThrow(new DataIntegrityViolationException(""));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminTagService.create(requestDTO);
        });
        assertEquals(ErrorCode.ALREADY_EXIST_TAG, exception.getErrorCode());
    }


    @Test
    @DisplayName("getAll()은 모든 태그를 성공적으로 조회한다")
    void getAll_success() {
        // given
        Tag tag1 = Tag.builder().id(1L).tagName("minimal").priority(1).tagNameKr("미니멀").tagPrompt("a minimal mood").build();
        Tag tag2 = Tag.builder().id(2L).tagName("modern").priority(2).tagNameKr("모던").tagPrompt("a modern mood").build();
        when(tagRepository.findAllByOrderByPriorityAsc()).thenReturn(List.of(tag1, tag2));

        // when
        AdminTagGetAllResponseDTO result = adminTagService.getAll();

        // then
        assertThat(result.tagGetResponseDTOS()).hasSize(2);
        assertThat(result.tagGetResponseDTOS().get(0).tagName()).isEqualTo("minimal");
        assertThat(result.tagGetResponseDTOS().get(1).tagName()).isEqualTo("modern");
    }


    @Test
    @DisplayName("update()는 태그를 성공적으로 업데이트한다")
    void update_success() {
        // given
        AdminTagUpdateRequestDTO requestDTO = new AdminTagUpdateRequestDTO("미니멀", 2, "new minimal", "a new minimal mood");
        Tag tag = Tag.builder().tagName("minimal").priority(1).tagNameKr("미니멀").tagPrompt("a minimal mood").build();

        when(tagRepository.findByTagNameKr("미니멀")).thenReturn(Optional.of(tag));
        when(tagRepository.findByPriority(2)).thenReturn(Optional.empty());

        // when
        adminTagService.update(requestDTO);

        // then
        assertThat(tag.getTagName()).isEqualTo("new minimal");
        assertThat(tag.getPriority()).isEqualTo(2);
        assertThat(tag.getTagPrompt()).isEqualTo("a new minimal mood");
    }


    @Test
    @DisplayName("update()는 존재하지 않는 태그 이름으로 업데이트 시 예외를 발생시킨다")
    void update_tagNotFound() {
        // given
        AdminTagUpdateRequestDTO requestDTO = new AdminTagUpdateRequestDTO("없는 태그", 2, "new minimal", "a new minimal mood");
        when(tagRepository.findByTagNameKr("없는 태그")).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminTagService.update(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_TAG_ENTITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("update()는 이미 존재하는 우선순위로 업데이트 시 예외를 발생시킨다")
    void update_alreadyExistPriority() {
        // given
        AdminTagUpdateRequestDTO requestDTO = new AdminTagUpdateRequestDTO("미니멀", 2, "new minimal", "a new minimal mood");
        Tag tag = Tag.builder().tagName("minimal").priority(1).tagNameKr("미니멀").tagPrompt("a minimal mood").build();

        when(tagRepository.findByTagNameKr("미니멀")).thenReturn(Optional.of(tag));
        when(tagRepository.findByPriority(2)).thenReturn(Optional.of(Tag.builder().build()));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminTagService.update(requestDTO);
        });
        assertEquals(ErrorCode.ALREADY_EXIST_PRIORITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("delete()는 태그를 성공적으로 삭제한다")
    void delete_success() {
        // given
        AdminTagDeleteRequestDTO requestDTO = new AdminTagDeleteRequestDTO("미니멀");
        Tag tag = Tag.builder().build();
        when(tagRepository.findByTagNameKr("미니멀")).thenReturn(Optional.of(tag));

        // when
        adminTagService.delete(requestDTO);

        // then
        verify(tagRepository, times(1)).delete(tag);
    }


    @Test
    @DisplayName("delete()는 존재하지 않는 태그 이름으로 삭제 시 예외를 발생시킨다")
    void delete_tagNotFound() {
        // given
        AdminTagDeleteRequestDTO requestDTO = new AdminTagDeleteRequestDTO("없는 태그");
        when(tagRepository.findByTagNameKr("없는 태그")).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminTagService.delete(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_TAG_ENTITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("delete()는 외래키 제약 조건 위반 시 예외를 발생시킨다")
    void delete_foreignKeyConstraintFail() {
        // given
        AdminTagDeleteRequestDTO requestDTO = new AdminTagDeleteRequestDTO("미니멀");
        Tag tag = Tag.builder().build();
        when(tagRepository.findByTagNameKr("미니멀")).thenReturn(Optional.of(tag));
        doThrow(new DataIntegrityViolationException("")).when(tagRepository).delete(tag);

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminTagService.delete(requestDTO);
        });
        assertEquals(ErrorCode.FOREIGN_KEY_CONSTRAINT_FAIL, exception.getErrorCode());
    }
}
