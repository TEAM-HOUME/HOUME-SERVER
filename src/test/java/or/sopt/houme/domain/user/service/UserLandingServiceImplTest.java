package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.presentation.controller.dto.LandingListResponse;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TokenException;
import or.sopt.houme.global.api.handler.UserException;
import or.sopt.houme.global.jwt.JWTUtil;
import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLandingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private BannerRepository bannerRepository;

    @InjectMocks
    private UserLandingServiceImpl userLandingService;

    @Test
    @DisplayName("랜딩 목록 조회 시 배너를 랜딩 응답으로 변환한다")
    void getLandings_returnsBanners() {
        Banner firstBanner = Banner.builder()
                .id(1L)
                .bannerTitle("재택근무가 필요한")
                .bannerImageUrl("https://google.com")
                .build();
        Banner secondBanner = Banner.builder()
                .id(2L)
                .bannerTitle("취향 탐색이 필요한")
                .bannerImageUrl("https://google.com")
                .build();
        when(bannerRepository.findAll(Sort.by(Sort.Direction.ASC, "id")))
                .thenReturn(List.of(firstBanner, secondBanner));

        LandingListResponse result = userLandingService.getLandings();

        assertEquals(2, result.landings().size());
        assertEquals(1L, result.landings().get(0).id());
        assertEquals("재택근무가 필요한", result.landings().get(0).name());
        assertEquals("https://google.com", result.landings().get(0).imageUrl());
    }

    @Test
    @DisplayName("쿠키가 없으면 true 반환")
    void getHasGeneratedImage_noCookies_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        Boolean result = userLandingService.getHasGeneratedImage(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("리프레시 토큰이 없으면 true 반환")
    void getHasGeneratedImage_noRefreshToken_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("other-cookie", "value")};
        when(request.getCookies()).thenReturn(cookies);

        Boolean result = userLandingService.getHasGeneratedImage(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("리프레시 토큰이 존재하지 않으면 예외 발생")
    void getHasGeneratedImage_invalidRefreshToken_throwsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("refresh-token", "fakeToken")};
        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.getId("fakeToken")).thenReturn(1L);
        when(refreshTokenRepository.existsById(1L)).thenReturn(false);

        TokenException e = assertThrows(TokenException.class, () ->
                userLandingService.getHasGeneratedImage(request)
        );

        assertEquals(ErrorCode.REFRESH_TOKEN_NULL, e.getErrorCode());
    }

    @Test
    @DisplayName("유저가 없으면 USER_NOT_FOUND 예외 발생")
    void getHasGeneratedImage_userNotFound_throwsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("refresh-token", "token")};
        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.getId("token")).thenReturn(99L);
        when(refreshTokenRepository.existsById(99L)).thenReturn(true);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserException e = assertThrows(UserException.class, () ->
                userLandingService.getHasGeneratedImage(request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("유저가 이미 이미지를 생성한 경우 false 반환")
    void getHasGeneratedImage_userHasImage_returnsFalse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("refresh-token", "token")};
        when(request.getCookies()).thenReturn(cookies);

        User mockUser = User.builder()
                .id(1L)
                .hasGeneratedImage(true)
                .build();

        when(jwtUtil.getId("token")).thenReturn(1L);
        when(refreshTokenRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Boolean result = userLandingService.getHasGeneratedImage(request);

        assertFalse(result);
    }

    @Test
    @DisplayName("유저가 이미지를 생성하지 않은 경우 true 반환")
    void getHasGeneratedImage_userHasNotImage_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = {new Cookie("refresh-token", "token")};
        when(request.getCookies()).thenReturn(cookies);

        User mockUser = User.builder()
                .id(1L)
                .hasGeneratedImage(false)
                .build();

        when(jwtUtil.getId("token")).thenReturn(1L);
        when(refreshTokenRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Boolean result = userLandingService.getHasGeneratedImage(request);

        assertTrue(result);
    }
}
