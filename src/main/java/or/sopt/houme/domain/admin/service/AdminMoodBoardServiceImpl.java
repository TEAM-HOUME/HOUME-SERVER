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



    @Override
    public AdminMoodBoardCreateResponseDTO create(AdminMoodBoardCreateRequestDTO requestDTO, String contentType) {

        S3PresignedUrlResponseDTO presignedUrl = s3PresignedUtil.createPresignedUrl(requestDTO.imageExtension(), "moodboard", contentType);

        Taste taste = Taste.builder()
                .url(presignedUrl.publicUrl())
                .filename(presignedUrl.keyName())
                .originalFilename(requestDTO.originalFilename())
                .fileExtension(requestDTO.imageExtension())
                .build();

        Tag byIdTag = tagRepository.findById(requestDTO.tagId())
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TAG_ENTITY));

        TasteTag newTasteTag = TasteTag.builder()
                .taste(taste)
                .tag(byIdTag)
                .build();

        tasteTagRepository.save(newTasteTag);
        Taste savedTaste = tasteRepository.save(taste);

        return new AdminMoodBoardCreateResponseDTO(presignedUrl.uploadUrl(), savedTaste.getId());
    }


    @Override
    public AdminMoodBoardGetAllResponseDTO getAll() {

        List<AdminMoodBoardGetResponseDTO> moodBoardList = tasteRepository.findAll().stream()
                .map(taste -> new AdminMoodBoardGetResponseDTO(
                        taste.getFilename(),
                        taste.getOriginalFilename(),
                        taste.getUrl()
                )).toList();

        return new AdminMoodBoardGetAllResponseDTO(moodBoardList);
    }


    @Override
    public void delete(String filename){

        log.info(filename);

        Taste byFilename = tasteRepository.findByFilename(filename)
                .orElseThrow(()-> new GeneralException(ErrorCode.NOT_FOUND_TASTE));

        TasteTag byTaste = tasteTagRepository.findByTaste(byFilename)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_FOUND_TASTE));

        tasteTagRepository.delete(byTaste);
        tasteRepository.delete(byFilename);

        s3Util.delete(filename);

    }
}