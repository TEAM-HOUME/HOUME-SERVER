package or.sopt.houme.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateResponseDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardGetAllResponseDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardGetResponseDTO;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminMoodBoardServiceImpl implements AdminMoodBoardService {



    private final S3PresignedUtil s3PresignedUtil;
    private final S3Util s3Util;
    private final TasteRepository tasteRepository;
    private final TagRepository tagRepository;
    private final TasteTagRepository tasteTagRepository;



    /**
     * s3PresignedUtil을 이용해서 무드보드 이미지를 저장하는 메서드입니다
     *
     * @throws GeneralException 이미지 업로드 중 에러가 발생하면 예외 발생
     * @throws GeneralException 태그 엔티티를 찾지 못하면 예외 발생
     * @throws DataIntegrityViolationException 데이터 저장 중, 데이터 무결성을 만족하지 못하면 예외 발생
     * */
    @Override
    public AdminMoodBoardCreateResponseDTO create(AdminMoodBoardCreateRequestDTO requestDTO, String contentType) {

        // 1. presinged URL을 발급받아서 반환함
        S3PresignedUrlResponseDTO presignedUrl;

        try {
            presignedUrl = s3PresignedUtil.createPresignedUrl(requestDTO.imageExtension(), "moodboard", contentType);
        }catch (Exception e) {
            throw new GeneralException(ErrorCode.IMAGE_UPLOAD_AMAZON_EXCEPTION);
        }

        // 2. 이미지를 기반으로 taste와 tag 데이터를 생성 및 조회
        Taste taste = Taste.createByPreSignedURL(presignedUrl,requestDTO);
        Tag byIdTag = tagRepository.findById(requestDTO.tagId())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        TasteTag newTasteTag = TasteTag.of(taste,byIdTag);
        Taste savedTaste;

        try {
            savedTaste = tasteRepository.save(taste);
            tasteTagRepository.save(newTasteTag);
        }catch (DataIntegrityViolationException e){
            throw new GeneralException(ErrorCode.DATA_INTEGRITY_VIOLATION);
        }

        return new AdminMoodBoardCreateResponseDTO(presignedUrl.uploadUrl(), savedTaste.getId());
    }


    /**
     * 무드보드를 모두 조회합니다.
     *
     * 매핑 테이블 조회가 없기 때문에 최적화할 요소가 존재하지 않습니다.
     * */
    @Override
    public AdminMoodBoardGetAllResponseDTO getAll() {

        List<AdminMoodBoardGetResponseDTO> moodBoardList = tasteRepository.findAll().stream()
                .map(taste -> AdminMoodBoardGetResponseDTO.of(
                        taste.getFilename(),
                        taste.getOriginalFilename(),
                        taste.getUrl()
                )).toList();

        return new AdminMoodBoardGetAllResponseDTO(moodBoardList);
    }


    /**
     * 무드보드 데이터를 삭제합니다.
     *
     * @throws GeneralException 무드보드 이미지 데이터를 찾지 못하면 예외 발생
     * @throws GeneralException 무드보드와 스타일 태그의 매핑데이터를 찾지 못하면 예외 발생
     * @throws DataIntegrityViolationException flush 시점에 데이터가 중복되면 예외 발생
     * @throws GeneralException 이미지 삭제 중 오류가 발생하면 예외 발생
     * */
    @Override
    public void delete(String filename){

        Taste byFilename = tasteRepository.findByFilename(filename)
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TASTE));

        TasteTag byTaste = tasteTagRepository.findByTaste(byFilename)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_TASTE));

        try {
            tasteTagRepository.delete(byTaste);
            tasteRepository.delete(byFilename);
        }catch (DataIntegrityViolationException e){
            throw new GeneralException(ErrorCode.DATA_INTEGRITY_VIOLATION);
        }

        try {
            s3Util.delete(filename);
        }catch (Exception e) {
            throw new GeneralException(ErrorCode.IMAGE_DELETE_EXCEPTION);
        }

    }
}