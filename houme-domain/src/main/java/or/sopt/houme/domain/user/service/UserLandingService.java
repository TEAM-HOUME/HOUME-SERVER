package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;

public interface UserLandingService {
    Boolean getHasGeneratedImage(HttpServletRequest request);
}
