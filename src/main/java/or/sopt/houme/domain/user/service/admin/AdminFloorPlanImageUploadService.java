package or.sopt.houme.domain.user.service.admin;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageUploadResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;
import or.sopt.houme.global.util.S3PresignedUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminFloorPlanImageUploadService {

    private static final Map<String, String> IMAGE_CONTENT_TYPES = createImageContentTypes();

    private final S3PresignedUtil s3PresignedUtil;

    public AdminFloorPlanImageUploadResponse createImageUploadUrl(
            AdminFloorPlanImageUploadRequest request,
            String contentType
    ) {
        String normalizedExtension = normalizeRequired(request.imageExtension()).toLowerCase();
        String normalizedContentType = validateImageContentType(normalizedExtension, contentType);
        S3PresignedUrlResponseDTO presignedUrl = s3PresignedUtil.createPresignedUrl(
                normalizedExtension,
                "floorplan",
                normalizedContentType
        );
        return new AdminFloorPlanImageUploadResponse(presignedUrl.uploadUrl(), presignedUrl.publicUrl());
    }

    private String validateImageContentType(String imageExtension, String contentType) {
        String expectedContentType = IMAGE_CONTENT_TYPES.get(imageExtension);
        String normalizedContentType = normalizeOptional(contentType);

        if (expectedContentType == null || normalizedContentType == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        String lowerContentType = normalizedContentType.toLowerCase();
        if (!expectedContentType.equals(lowerContentType)) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        return lowerContentType;
    }

    private String normalizeRequired(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Map<String, String> createImageContentTypes() {
        Map<String, String> contentTypes = new HashMap<>();
        contentTypes.put("jpg", "image/jpeg");
        contentTypes.put("jpeg", "image/jpeg");
        contentTypes.put("png", "image/png");
        contentTypes.put("gif", "image/gif");
        contentTypes.put("webp", "image/webp");
        return Map.copyOf(contentTypes);
    }
}
