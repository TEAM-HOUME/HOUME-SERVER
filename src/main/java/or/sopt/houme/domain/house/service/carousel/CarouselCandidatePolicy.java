package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;

import java.util.List;

public final class CarouselCandidatePolicy {

    public static final int SELECTED_FURNITURE_CANDIDATE_LIMIT = 160;
    public static final int FURNITURE_CATEGORY_CANDIDATE_LIMIT = 160;
    public static final int OTHER_CATEGORY_CANDIDATE_LIMIT = 60;
    public static final int FALLBACK_CANDIDATE_LIMIT = 220;

    public static final List<SoozipCategory> OTHER_CATEGORIES = List.of(
            SoozipCategory.LIGHTING,
            SoozipCategory.LIVING_GOODS,
            SoozipCategory.HOME_FABRIC,
            SoozipCategory.ACCESSORY,
            SoozipCategory.MINI_ELECTRONICS
    );

    private CarouselCandidatePolicy() {
    }
}
