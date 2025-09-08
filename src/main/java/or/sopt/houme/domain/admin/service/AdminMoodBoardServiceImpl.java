package or.sopt.houme.domain.admin.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateRequestDTO;
import or.sopt.houme.domain.admin.controller.dto.moodboard.AdminMoodBoardCreateResponseDTO;
import or.sopt.houme.domain.taste.entity.Taste;
import or.sopt.houme.domain.taste.repository.taste.TasteRepository;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;
import or.sopt.houme.global.util.S3PresignedUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminMoodBoardServiceImpl implements AdminMoodBoardService {

    private final S3PresignedUtil s3PresignedUtil;
    private final TasteRepository tasteRepository;

    @Override
    public AdminMoodBoardCreateResponseDTO create(AdminMoodBoardCreateRequestDTO requestDTO, String contentType) {
        S3PresignedUrlResponseDTO presignedUrl = s3PresignedUtil.createPresignedUrl(requestDTO.imageExtension(), "taste", contentType);

        Taste taste = Taste.builder()
                .url(presignedUrl.publicUrl())
                .filename(requestDTO.filename())
                .originalFilename(requestDTO.originalFilename())
                .fileExtension(requestDTO.imageExtension())
                .build();

        Taste savedTaste = tasteRepository.save(taste);

        return new AdminMoodBoardCreateResponseDTO(presignedUrl.uploadUrl(), savedTaste.getId());
    }
}