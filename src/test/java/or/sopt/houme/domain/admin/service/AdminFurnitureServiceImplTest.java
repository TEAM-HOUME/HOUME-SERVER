package or.sopt.houme.domain.admin.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import or.sopt.houme.domain.admin.controller.dto.furniture.*;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.entity.FurnitureType;
import or.sopt.houme.domain.furniture.entity.FurnitureTypes;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFurnitureServiceImplTest {

    @InjectMocks
    private AdminFurnitureServiceImpl adminFurnitureService;

    @Mock
    private FurnitureRepository furnitureRepository;

    @Mock
    private FurnitureTagRepository furnitureTagRepository;

    @Mock
    private FurnitureTypeRepository furnitureTypeRepository;

    @Mock
    private TagRepository tagRepository;

    private FurnitureType bedType;

    @BeforeEach
    void setUp() {

        bedType = FurnitureType.builder()
                .id(1L)
                .furnitureType(FurnitureTypes.BED)
                .build();

        FurnitureType sofaType = FurnitureType.builder()
                .id(2L)
                .furnitureType(FurnitureTypes.SELECTIVE)
                .build();
    }


    @Test
    @DisplayName("registerFurniture()는 새로운 가구를 성공적으로 등록한다")
    void registerFurniture_success() {
        // given
        AdminFurnitureRequestDTO requestDTO = new AdminFurnitureRequestDTO("새로운 침대", "new bed", true);
        when(furnitureRepository.findByFurnitureNameKr("새로운 침대")).thenReturn(Optional.empty());
        when(furnitureTypeRepository.findById(1L)).thenReturn(Optional.of(bedType));

        // when
        adminFurnitureService.registerFurniture(requestDTO);

        // then
        verify(furnitureRepository, times(1)).save(any(Furniture.class));
    }


    @Test
    @DisplayName("registerFurniture()는 이미 존재하는 가구 이름으로 등록 시 예외를 발생시킨다")
    void registerFurniture_alreadyExists() {
        // given
        AdminFurnitureRequestDTO requestDTO = new AdminFurnitureRequestDTO("기존 침대", "existing bed", true);
        when(furnitureRepository.findByFurnitureNameKr("기존 침대")).thenReturn(Optional.of(Furniture.builder().build()));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.registerFurniture(requestDTO);
        });
        assertEquals(ErrorCode.ALREADY_EXIST_FURNITURE, exception.getErrorCode());
    }


    @Test
    @DisplayName("registerFurniturePrompt()는 가구 프롬프트를 성공적으로 등록한다")
    void registerFurniturePrompt_success() {
        // given
        AdminFurniturePromptRequestDTO requestDTO = new AdminFurniturePromptRequestDTO("테스트 가구", "테스트 프롬프트", 1L);
        Tag tag = Tag.builder().id(1L).build();
        Furniture furniture = Furniture.builder().furnitureNameKr("테스트 가구").build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureRepository.findByFurnitureNameKr("테스트 가구")).thenReturn(Optional.of(furniture));

        // when
        adminFurnitureService.registerFurniturePrompt(requestDTO);

        // then
        verify(furnitureTagRepository, times(1)).save(any(FurnitureTag.class));
    }


    @Test
    @DisplayName("registerFurniturePrompt()는 존재하지 않는 태그 ID로 등록 시 예외를 발생시킨다")
    void registerFurniturePrompt_tagNotFound() {
        // given
        AdminFurniturePromptRequestDTO requestDTO = new AdminFurniturePromptRequestDTO("테스트 가구", "테스트 프롬프트", 99L);
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.registerFurniturePrompt(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_TAG_ENTITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("registerFurniturePrompt()는 존재하지 않는 가구 이름으로 등록 시 예외를 발생시킨다")
    void registerFurniturePrompt_furnitureNotFound() {
        // given
        AdminFurniturePromptRequestDTO requestDTO = new AdminFurniturePromptRequestDTO("없는 가구", "테스트 프롬프트", 1L);
        Tag tag = Tag.builder().id(1L).build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureRepository.findByFurnitureNameKr("없는 가구")).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.registerFurniturePrompt(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_FURNITURE, exception.getErrorCode());
    }


    @Test
    @DisplayName("getFurniture()는 모든 가구 정보를 성공적으로 조회한다")
    void getFurniture_success() {
        // given
        Furniture furniture1 = Furniture.builder().id(1L).furnitureNameKr("침대").build();
        Furniture furniture2 = Furniture.builder().id(2L).furnitureNameKr("소파").build();
        List<Furniture> furnitures = List.of(furniture1, furniture2);

        Tag tag1 = Tag.builder().id(1L).tagNameKr("모던").build();
        FurnitureTag furnitureTag1 = FurnitureTag.builder().furniture(furniture1).tag(tag1).build();
        when(furnitureTagRepository.findByFurniture(furniture1)).thenReturn(List.of(furnitureTag1));
        when(furnitureTagRepository.findByFurniture(furniture2)).thenReturn(Collections.emptyList());

        when(furnitureRepository.findAll()).thenReturn(furnitures);

        // when
        AdminFurnitureGetDTO result = adminFurnitureService.getFurniture();

        // then
        assertThat(result.furnitures()).hasSize(2);
        assertThat(result.furnitures().get(0).furnitureNameKr()).isEqualTo("침대");
        assertThat(result.furnitures().get(0).tags()).hasSize(1);
        assertThat(result.furnitures().get(0).tags().get(0).tagName()).isEqualTo("모던");
        assertThat(result.furnitures().get(1).furnitureNameKr()).isEqualTo("소파");
        assertThat(result.furnitures().get(1).tags()).isEmpty();
    }


    @Test
    @DisplayName("getFurnitureTag()는 모든 가구 태그 정보를 성공적으로 조회한다")
    void getFurnitureTag_success() {
        // given
        Tag tag1 = Tag.builder().id(1L).tagNameKr("모던").build();
        Tag tag2 = Tag.builder().id(2L).tagNameKr("미니멀").build();
        List<Tag> tags = List.of(tag1, tag2);

        when(tagRepository.findAll()).thenReturn(tags);

        // when
        AdminFurnitureTagGetDTO result = adminFurnitureService.getFurnitureTag();

        // then
        assertThat(result.tagId()).hasSize(2);
        assertThat(result.tagId()).containsExactly(1L, 2L);
        assertThat(result.tagNameKr()).hasSize(2);
        assertThat(result.tagNameKr()).containsExactly("모던", "미니멀");
    }


    @Test
    @DisplayName("updateFurniture()는 가구 정보를 성공적으로 업데이트한다")
    void updateFurniture_success() {
        // given
        AdminFurnitureUpdateRequestDTO requestDTO = new AdminFurnitureUpdateRequestDTO("침대", 1L, "new bed eng", "new prompt");
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();
        Tag tag = Tag.builder().id(1L).build();
        FurnitureTag furnitureTag = FurnitureTag.builder().furniture(furniture).tag(tag).build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureTagRepository.findByFurnitureAndTag(furniture, tag)).thenReturn(Optional.of(furnitureTag));

        // when
        adminFurnitureService.updateFurniture(requestDTO);

        // then
        assertThat(furniture.getFurnitureNameEng()).isEqualTo("new bed eng");
        assertThat(furnitureTag.getFurniturePrompt()).isEqualTo("new prompt");
    }


    @Test
    @DisplayName("updateFurniture()는 존재하지 않는 가구 이름으로 업데이트 시 예외를 발생시킨다")
    void updateFurniture_furnitureNotFound() {
        // given
        AdminFurnitureUpdateRequestDTO requestDTO = new AdminFurnitureUpdateRequestDTO("없는 가구", 1L, "new bed eng", "new prompt");
        when(furnitureRepository.findByFurnitureNameKr("없는 가구")).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.updateFurniture(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_FURNITURE, exception.getErrorCode());
    }


    @Test
    @DisplayName("updateFurniture()는 존재하지 않는 태그 ID로 업데이트 시 예외를 발생시킨다")
    void updateFurniture_tagNotFound() {
        // given
        AdminFurnitureUpdateRequestDTO requestDTO = new AdminFurnitureUpdateRequestDTO("침대", 99L, "new bed eng", "new prompt");
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.updateFurniture(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_TAG_ENTITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("updateFurniture()는 존재하지 않는 가구-태그 매핑으로 업데이트 시 예외를 발생시킨다")
    void updateFurniture_furnitureTagNotFound() {
        // given
        AdminFurnitureUpdateRequestDTO requestDTO = new AdminFurnitureUpdateRequestDTO("침대", 1L, "new bed eng", "new prompt");
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();
        Tag tag = Tag.builder().id(1L).build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureTagRepository.findByFurnitureAndTag(furniture, tag)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.updateFurniture(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_FURNITURE_TAG, exception.getErrorCode());
    }


    @Test
    @DisplayName("deleteFurnitureTag()는 가구-태그 매핑을 성공적으로 삭제한다")
    void deleteFurnitureTag_success() {
        // given
        AdminFurnitureTagDeleteDTO requestDTO = new AdminFurnitureTagDeleteDTO("침대", 1L);
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();
        Tag tag = Tag.builder().id(1L).build();
        FurnitureTag furnitureTag = FurnitureTag.builder().furniture(furniture).tag(tag).build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureTagRepository.findByFurnitureAndTag(furniture, tag)).thenReturn(Optional.of(furnitureTag));

        // when
        adminFurnitureService.deleteFurnitureTag(requestDTO);

        // then
        verify(furnitureTagRepository, times(1)).delete(furnitureTag);
    }


    @Test
    @DisplayName("deleteFurnitureTag()는 존재하지 않는 가구 이름으로 삭제 시 예외를 발생시킨다")
    void deleteFurnitureTag_furnitureNotFound() {
        // given
        AdminFurnitureTagDeleteDTO requestDTO = new AdminFurnitureTagDeleteDTO("없는 가구", 1L);
        when(furnitureRepository.findByFurnitureNameKr("없는 가구")).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.deleteFurnitureTag(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_FURNITURE, exception.getErrorCode());
    }


    @Test
    @DisplayName("deleteFurnitureTag()는 존재하지 않는 태그 ID로 삭제 시 예외를 발생시킨다")
    void deleteFurnitureTag_tagNotFound() {
        // given
        AdminFurnitureTagDeleteDTO requestDTO = new AdminFurnitureTagDeleteDTO("침대", 99L);
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.deleteFurnitureTag(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_TAG_ENTITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("deleteFurnitureTag()는 존재하지 않는 가구-태그 매핑으로 삭제 시 예외를 발생시킨다")
    void deleteFurnitureTag_furnitureTagNotFound() {
        // given
        AdminFurnitureTagDeleteDTO requestDTO = new AdminFurnitureTagDeleteDTO("침대", 1L);
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();
        Tag tag = Tag.builder().id(1L).build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureTagRepository.findByFurnitureAndTag(furniture, tag)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.deleteFurnitureTag(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_FURNITURE_TAG, exception.getErrorCode());
    }


    @Test
    @DisplayName("deleteFurniture()는 가구를 성공적으로 삭제한다")
    void deleteFurniture_success() {
        // given
        AdminFurnitureDeleteDTO requestDTO = new AdminFurnitureDeleteDTO("침대");
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(furnitureTagRepository.findByFurniture(furniture)).thenReturn(Collections.emptyList());

        // when
        adminFurnitureService.deleteFurniture(requestDTO);

        // then
        verify(furnitureRepository, times(1)).delete(furniture);
    }


    @Test
    @DisplayName("deleteFurniture()는 존재하지 않는 가구 이름으로 삭제 시 예외를 발생시킨다")
    void deleteFurniture_furnitureNotFound() {
        //.given
        AdminFurnitureDeleteDTO requestDTO = new AdminFurnitureDeleteDTO("없는 가구");
        when(furnitureRepository.findByFurnitureNameKr("없는 가구")).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.deleteFurniture(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_FURNITURE, exception.getErrorCode());
    }


    @Test
    @DisplayName("deleteFurniture()는 가구에 태그가 매핑되어 있을 경우 예외를 발생시킨다")
    void deleteFurniture_invalidDelete() {
        // given
        AdminFurnitureDeleteDTO requestDTO = new AdminFurnitureDeleteDTO("침대");
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();
        FurnitureTag furnitureTag = FurnitureTag.builder().build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(furnitureTagRepository.findByFurniture(furniture)).thenReturn(List.of(furnitureTag));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.deleteFurniture(requestDTO);
        });
        assertEquals(ErrorCode.INVALID_DELETE_FURNITURE, exception.getErrorCode());
    }


    @Test
    @DisplayName("getDetails()는 가구 상세 정보를 성공적으로 조회한다")
    void getDetails_success() {
        // given
        AdminFurnitureDetailsRequestDTO requestDTO = new AdminFurnitureDetailsRequestDTO("침대", 1L);
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();
        Tag tag = Tag.builder().id(1L).build();
        FurnitureTag furnitureTag = FurnitureTag.builder().furniture(furniture).tag(tag).furniturePrompt("테스트 프롬프트").build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureTagRepository.findByFurnitureAndTag(furniture, tag)).thenReturn(Optional.of(furnitureTag));

        // when
        AdminFurnitureDetailsResponseDTO result = adminFurnitureService.getDetails(requestDTO);

        // then
        assertThat(result.prompt()).isEqualTo("테스트 프롬프트");
    }


    @Test
    @DisplayName("getDetails()는 존재하지 않는 가구 이름으로 조회 시 예외를 발생시킨다")
    void getDetails_furnitureNotFound() {
        // given
        AdminFurnitureDetailsRequestDTO requestDTO = new AdminFurnitureDetailsRequestDTO("없는 가구", 1L);
        when(furnitureRepository.findByFurnitureNameKr("없는 가구")).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.getDetails(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_FURNITURE, exception.getErrorCode());
    }


    @Test
    @DisplayName("getDetails()는 존재하지 않는 태그 ID로 조회 시 예외를 발생시킨다")
    void getDetails_tagNotFound() {
        // given
        AdminFurnitureDetailsRequestDTO requestDTO = new AdminFurnitureDetailsRequestDTO("침대", 99L);
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.getDetails(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_TAG_ENTITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("getDetails()는 존재하지 않는 가구-태그 매핑으로 조회 시 예외를 발생시킨다")
    void getDetails_furnitureTagNotFound() {
        // given
        AdminFurnitureDetailsRequestDTO requestDTO = new AdminFurnitureDetailsRequestDTO("침대", 1L);
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();
        Tag tag = Tag.builder().id(1L).build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureTagRepository.findByFurnitureAndTag(furniture, tag)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.getDetails(requestDTO);
        });
        assertEquals(ErrorCode.NOT_FOUND_FURNITURE_TAG, exception.getErrorCode());
    }


    @Test
    @DisplayName("registerFurniture()는 존재하지 않는 가구 타입으로 등록 시 예외를 발생시킨다")
    void registerFurniture_notValidException() {
        // given
        AdminFurnitureRequestDTO requestDTO = new AdminFurnitureRequestDTO("새로운 침대", "new bed", true);
        when(furnitureRepository.findByFurnitureNameKr("새로운 침대")).thenReturn(Optional.empty());
        when(furnitureTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.registerFurniture(requestDTO);
        });
        assertEquals(ErrorCode.NOT_VALID_EXCEPTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("deleteFurnitureTag()는 외래키 제약 조건 위반 시 예외를 발생시킨다")
    void deleteFurnitureTag_foreignKeyConstraintFail() {
        // given
        AdminFurnitureTagDeleteDTO requestDTO = new AdminFurnitureTagDeleteDTO("침대", 1L);
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();
        Tag tag = Tag.builder().id(1L).build();
        FurnitureTag furnitureTag = FurnitureTag.builder().furniture(furniture).tag(tag).build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(furnitureTagRepository.findByFurnitureAndTag(furniture, tag)).thenReturn(Optional.of(furnitureTag));
        doThrow(new DataIntegrityViolationException("")).when(furnitureTagRepository).delete(furnitureTag);

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.deleteFurnitureTag(requestDTO);
        });
        assertEquals(ErrorCode.FOREIGN_KEY_CONSTRAINT_FAIL, exception.getErrorCode());
    }


    @Test
    @DisplayName("deleteFurniture()는 외래키 제약 조건 위반 시 예외를 발생시킨다")
    void deleteFurniture_foreignKeyConstraintFail() {
        // given
        AdminFurnitureDeleteDTO requestDTO = new AdminFurnitureDeleteDTO("침대");
        Furniture furniture = Furniture.builder().furnitureNameKr("침대").build();

        when(furnitureRepository.findByFurnitureNameKr("침대")).thenReturn(Optional.of(furniture));
        when(furnitureTagRepository.findByFurniture(furniture)).thenReturn(Collections.emptyList());
        doThrow(new DataIntegrityViolationException("")).when(furnitureRepository).delete(furniture);

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminFurnitureService.deleteFurniture(requestDTO);
        });
        assertEquals(ErrorCode.FOREIGN_KEY_CONSTRAINT_FAIL, exception.getErrorCode());
    }
}
