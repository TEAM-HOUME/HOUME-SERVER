package or.sopt.houme.domain.user.controller;

import or.sopt.houme.domain.user.service.UserLandingService;
import or.sopt.houme.global.api.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserLandingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserLandingService userLandingService;

    @Test
    @DisplayName("checkHasGeneratedImage()는 true를 반환한다")
    void checkHasGeneratedImage_true() throws Exception {
        // given
        Mockito.when(userLandingService.getHasGeneratedImage(any()))
                .thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/v1/check-has-generated-image")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("checkHasGeneratedImage()는 false를 반환한다")
    void checkHasGeneratedImage_false() throws Exception {
        // given
        Mockito.when(userLandingService.getHasGeneratedImage(any()))
                .thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/v1/check-has-generated-image")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }
}
