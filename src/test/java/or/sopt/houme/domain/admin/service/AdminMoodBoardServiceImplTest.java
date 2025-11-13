package or.sopt.houme.domain.admin.service;

import or.sopt.houme.domain.house.entity.mapping.HouseTaste;
import or.sopt.houme.domain.house.repository.HouseTasteRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateResponseDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardGetAllResponseDTO;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.domain.taste.entity.TasteTag;
import or.sopt.houme.domain.taste.repository.tag.TagRepository;
import or.sopt.houme.domain.taste.repository.taste.TasteRepository;
import or.sopt.houme.domain.taste.repository.taste_tag.TasteTagRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;
import or.sopt.houme.global.util.S3PresignedUtil;
import or.sopt.houme.global.util.S3Util;
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
class AdminMoodBoardServiceImplTest {

    @InjectMocks
    private AdminMoodBoardServiceImpl adminMoodBoardService;

    @Mock
    private S3PresignedUtil s3PresignedUtil;

    @Mock
    private S3Util s3Util;

    @Mock
    private TasteRepository tasteRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private HouseTasteRepository houseTasteRepository;

    @Mock
    private TasteTagRepository tasteTagRepository;

    @Test
    @DisplayName("create()는 무드보드를 성공적으로 생성한다")
    void create_success() {
        // given
        AdminMoodBoardCreateRequestDTO requestDTO = new AdminMoodBoardCreateRequestDTO("jpg", "image.jpg", 1L);
        S3PresignedUrlResponseDTO presignedUrl = new S3PresignedUrlResponseDTO("uploadUrl", "imageUrl", "key", "dir");
        Tag tag = Tag.builder().id(1L).build();
        Taste taste = Taste.builder().id(1L).build();

        when(s3PresignedUtil.createPresignedUrl(any(), any(), any())).thenReturn(presignedUrl);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tasteRepository.save(any(Taste.class))).thenReturn(taste);

        // when
        AdminMoodBoardCreateResponseDTO result = adminMoodBoardService.create(requestDTO, "image/jpeg");

        // then
        assertThat(result.presignedUrl()).isEqualTo("uploadUrl");
        assertThat(result.tasteId()).isEqualTo(1L);
        verify(tasteTagRepository, times(1)).save(any());
    }


    @Test
    @DisplayName("create()는 존재하지 않는 태그 ID로 생성 시 예외를 발생시킨다")
    void create_tagNotFound() {
        // given
        AdminMoodBoardCreateRequestDTO requestDTO = new AdminMoodBoardCreateRequestDTO("jpg", "image.jpg", 99L);
        S3PresignedUrlResponseDTO presignedUrl = new S3PresignedUrlResponseDTO("uploadUrl", "imageUrl", "key", "dir");

        when(s3PresignedUtil.createPresignedUrl(any(), any(), any())).thenReturn(presignedUrl);
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminMoodBoardService.create(requestDTO, "image/jpeg");
        });
        assertEquals(ErrorCode.NOT_FOUND_TAG_ENTITY, exception.getErrorCode());
    }


    @Test
    @DisplayName("getAll()은 모든 무드보드를 성공적으로 조회한다")
    void getAll_success() {
        // given
        Taste taste1 = Taste.builder().filename("file1").originalFilename("orig1").url("url1").build();
        Taste taste2 = Taste.builder().filename("file2").originalFilename("orig2").url("url2").build();
        when(tasteRepository.findAll()).thenReturn(List.of(taste1, taste2));

        // when
        AdminMoodBoardGetAllResponseDTO result = adminMoodBoardService.getAll();

        // then
        assertThat(result.dtos()).hasSize(2);
        assertThat(result.dtos().get(0).filename()).isEqualTo("file1");
        assertThat(result.dtos().get(1).filename()).isEqualTo("file2");
    }


    @Test
    @DisplayName("delete()는 무드보드를 성공적으로 삭제한다")
    void delete_success() {
        // given
        String filename = "test.jpg";
        Taste taste = Taste.builder().filename(filename).build();
        HouseTaste houseTaste = HouseTaste.builder().taste(taste).build();
        TasteTag tasteTag = TasteTag.builder().taste(taste).build();

        when(tasteRepository.findByFilename(filename)).thenReturn(Optional.of(taste));
        when(houseTasteRepository.findAllByTaste(taste)).thenReturn(List.of(houseTaste));
        when(tasteTagRepository.findByTaste(taste)).thenReturn(Optional.of(tasteTag));

        // when
        adminMoodBoardService.delete(filename);

        // then
        verify(tasteTagRepository, times(1)).delete(tasteTag);
        verify(tasteRepository, times(1)).delete(taste);
        verify(s3Util, times(1)).delete(filename);
    }


    @Test
    @DisplayName("delete()는 존재하지 않는 파일 이름으로 삭제 시 예외를 발생시킨다")
    void delete_tasteNotFound() {
        // given
        String filename = "notfound.jpg";
        when(tasteRepository.findByFilename(filename)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminMoodBoardService.delete(filename);
        });
        assertEquals(ErrorCode.NOT_FOUND_TASTE, exception.getErrorCode());
    }


    @Test
    @DisplayName("delete()는 존재하지 않는 무드보드-태그 매핑으로 삭제 시 예외를 발생시킨다")
    void delete_tasteTagNotFound() {
        // given
        String filename = "test.jpg";
        Taste taste = Taste.builder().filename(filename).build();
        HouseTaste houseTaste = HouseTaste.builder().taste(taste).build();

        when(tasteRepository.findByFilename(filename)).thenReturn(Optional.of(taste));
        when(houseTasteRepository.findAllByTaste(taste)).thenReturn(List.of(houseTaste));
        when(tasteTagRepository.findByTaste(taste)).thenReturn(Optional.empty());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminMoodBoardService.delete(filename);
        });
        assertEquals(ErrorCode.NOT_FOUND_TASTE, exception.getErrorCode());
    }


    @Test
    @DisplayName("create()는 이미지 업로드 중 예외 발생 시 예외를 발생시킨다")
    void create_imageUploadAmazonException() {
        // given
        AdminMoodBoardCreateRequestDTO requestDTO = new AdminMoodBoardCreateRequestDTO("jpg", "image.jpg", 1L);
        when(s3PresignedUtil.createPresignedUrl(any(), any(), any())).thenThrow(new RuntimeException());

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminMoodBoardService.create(requestDTO, "image/jpeg");
        });
        assertEquals(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION, exception.getErrorCode());
    }


    @Test
    @DisplayName("create()는 데이터 저장 중 무결성 제약 조건 위반 시 예외를 발생시킨다")
    void create_dataIntegrityViolation() {
        // given
        AdminMoodBoardCreateRequestDTO requestDTO = new AdminMoodBoardCreateRequestDTO("jpg", "image.jpg", 1L);
        S3PresignedUrlResponseDTO presignedUrl = new S3PresignedUrlResponseDTO("uploadUrl", "imageUrl", "key", "dir");
        Tag tag = Tag.builder().id(1L).build();

        when(s3PresignedUtil.createPresignedUrl(any(), any(), any())).thenReturn(presignedUrl);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tasteRepository.save(any(Taste.class))).thenThrow(new DataIntegrityViolationException(""));

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminMoodBoardService.create(requestDTO, "image/jpeg");
        });
        assertEquals(ErrorCode.DATA_INTEGRITY_VIOLATION, exception.getErrorCode());
    }


    @Test
    @DisplayName("delete()는 데이터 삭제 중 무결성 제약 조건 위반 시 예외를 발생시킨다")
    void delete_dataIntegrityViolation() {
        // given
        String filename = "test.jpg";
        Taste taste = Taste.builder().filename(filename).build();
        HouseTaste houseTaste = HouseTaste.builder().taste(taste).build();
        TasteTag tasteTag = TasteTag.builder().taste(taste).build();

        when(tasteRepository.findByFilename(filename)).thenReturn(Optional.of(taste));
        when(houseTasteRepository.findAllByTaste(taste)).thenReturn(List.of(houseTaste));
        when(tasteTagRepository.findByTaste(taste)).thenReturn(Optional.of(tasteTag));
        doThrow(new DataIntegrityViolationException("")).when(tasteTagRepository).delete(tasteTag);

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminMoodBoardService.delete(filename);
        });
        assertEquals(ErrorCode.DATA_INTEGRITY_VIOLATION, exception.getErrorCode());
    }


    @Test
    @DisplayName("delete()는 이미지 삭제 중 예외 발생 시 예외를 발생시킨다")
    void delete_imageDeleteException() {
        // given
        String filename = "test.jpg";
        Taste taste = Taste.builder().filename(filename).build();
        HouseTaste houseTaste = HouseTaste.builder().taste(taste).build();
        TasteTag tasteTag = TasteTag.builder().taste(taste).build();

        when(tasteRepository.findByFilename(filename)).thenReturn(Optional.of(taste));
        when(houseTasteRepository.findAllByTaste(taste)).thenReturn(List.of(houseTaste));
        when(tasteTagRepository.findByTaste(taste)).thenReturn(Optional.of(tasteTag));
        doThrow(new RuntimeException()).when(s3Util).delete(filename);

        // when & then
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            adminMoodBoardService.delete(filename);
        });
        assertEquals(ErrorCode.IMAGE_DELETE_EXCEPTION, exception.getErrorCode());
    }
}
