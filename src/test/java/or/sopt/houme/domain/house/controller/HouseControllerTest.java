package or.sopt.houme.domain.house.controller;

import or.sopt.houme.domain.house.HouseLikeFacade;
import or.sopt.houme.domain.house.dto.HouseOptionDTO;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.global.jwt.JWTFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@DisplayName("[House Controller Test]")
@ActiveProfiles("test")
@WebMvcTest(controllers = HouseController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JWTFilter.class
        })
})
class HouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HouseService houseService;

    @MockBean
    private HouseLikeFacade houseLikeFacade;

    @Test
    @WithMockUser   // 인증된 사용자로 요청
    @DisplayName("/housing-options가 집 구조(주거형태, 공간구조, 평형) 정보에 대한 옵션 리스트들을 반환한다.")
    void getHousingOptions() throws Exception {
        // Given
        List<HouseOptionDTO> housingTypes = List.of(new HouseOptionDTO("OFFICETEL", "오피스텔"));
        List<HouseOptionDTO> roomTypes = List.of(new HouseOptionDTO("OPEN_ONE_ROOM", "오픈형 원룸"));
        List<HouseOptionDTO> areaTypes = List.of(new HouseOptionDTO("UNDER_5", "5평 이하"));

        HouseOptionsResponse houseOptionsResponse = new HouseOptionsResponse(housingTypes, roomTypes, areaTypes);

        when(houseService.getHouseOptionsResponse()).thenReturn(houseOptionsResponse);

        // When // Then
        mockMvc.perform(get("/api/v1/housing-options"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.data.housingTypes").isArray())
                .andExpect(jsonPath("$.data.roomTypes").isArray())
                .andExpect(jsonPath("$.data.areaTypes").isArray())
                .andExpect(jsonPath("$.data.housingTypes[0].code").value("OFFICETEL"))
                .andExpect(jsonPath("$.data.housingTypes[0].description").value("오피스텔"))
                .andExpect(jsonPath("$.data.roomTypes[0].code").value("OPEN_ONE_ROOM"))
                .andExpect(jsonPath("$.data.roomTypes[0].description").value("오픈형 원룸"))
                .andExpect(jsonPath("$.data.areaTypes[0].code").value("UNDER_5"))
                .andExpect(jsonPath("$.data.areaTypes[0].description").value("5평 이하"));
    }
}
