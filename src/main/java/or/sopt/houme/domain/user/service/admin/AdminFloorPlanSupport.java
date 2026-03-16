package or.sopt.houme.domain.user.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageUploadResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;
import or.sopt.houme.global.util.S3PresignedUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminFloorPlanSupport {

    private static final TypeReference<List<FloorPlanImageItem>> FLOOR_PLAN_IMAGE_TYPE = new TypeReference<>() {};
    private static final Map<String, String> IMAGE_CONTENT_TYPES = createImageContentTypes();

    private final ObjectMapper objectMapper;
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

    public List<FloorPlanImageItem> normalizeImages(List<AdminFloorPlanImageRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }

        List<FloorPlanImageItem> items = requests.stream()
                .map(request -> {
                    if (request == null || request.sortOrder() == null || request.sortOrder() < 1) {
                        throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
                    }
                    return new FloorPlanImageItem(
                            normalizeRequired(request.url()),
                            normalizeRequired(request.filename()),
                            normalizeRequired(request.originalFilename()),
                            normalizeRequired(request.fileExtension()).toLowerCase(),
                            request.sortOrder()
                    );
                })
                .sorted(Comparator.comparing(FloorPlanImageItem::sortOrder))
                .toList();

        Set<Integer> sortOrders = new LinkedHashSet<>();
        if (items.stream().anyMatch(item -> !sortOrders.add(item.sortOrder()))) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return items;
    }

    public String toImagesJson(List<FloorPlanImageItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    public List<FloorPlanImageItem> parseImagesJson(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return List.of();
        }
        try {
            List<FloorPlanImageItem> items = objectMapper.readValue(imagesJson, FLOOR_PLAN_IMAGE_TYPE);
            if (items == null) {
                return List.of();
            }
            return items.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(FloorPlanImageItem::sortOrder))
                    .toList();
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION);
        }
    }

    public List<FloorPlanImageItem> resolveImages(FloorPlan floorPlan) {
        List<FloorPlanImageItem> items = parseImagesJson(floorPlan.getImagesJson());
        if (!items.isEmpty()) {
            return items;
        }
        if (floorPlan.getUrl() == null || floorPlan.getUrl().isBlank()) {
            return List.of();
        }
        List<FloorPlanImageItem> legacyItems = new ArrayList<>();
        legacyItems.add(new FloorPlanImageItem(
                floorPlan.getUrl(),
                floorPlan.getFilename(),
                floorPlan.getOriginalFilename(),
                floorPlan.getFileExtension(),
                1
        ));
        return legacyItems;
    }

    public FloorPlanImageItem extractRepresentativeImage(List<FloorPlanImageItem> items) {
        if (items == null || items.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return items.stream()
                .min(Comparator.comparing(FloorPlanImageItem::sortOrder))
                .orElseThrow(() -> new GeneralException(ErrorCode.NOT_VALID_EXCEPTION));
    }

    public String normalizeRequired(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return normalized;
    }

    public String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
