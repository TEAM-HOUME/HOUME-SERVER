package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import or.sopt.houme.domain.user.presentation.controller.dto.LandingListResponse;

public interface UserLandingService {
    Boolean getHasGeneratedImage(HttpServletRequest request);

    LandingListResponse getLandings();
}
