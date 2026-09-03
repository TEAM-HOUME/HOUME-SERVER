package or.sopt.houme.compare.application.dto;

import or.sopt.houme.compare.domain.ComparePresetView;

import java.util.List;

public record PresetListResponse(List<PresetItemResponse> presets) {

    public static PresetListResponse from(List<ComparePresetView> views) {
        return new PresetListResponse(
                views.stream()
                        .map(v -> new PresetItemResponse(v.presetId(), v.thumbnailUrl(), v.title()))
                        .toList()
        );
    }
}
