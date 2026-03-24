package or.sopt.houme.domain.user.service.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanResponse;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImagePresignedUrlGenerator;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminFloorPlanServiceImplTest {

    @InjectMocks
    private AdminFloorPlanServiceImpl adminFloorPlanService;

    @Mock
    private FloorPlanRepository floorPlanRepository;

    @Mock
    private FloorPlanImagePresignedUrlGenerator floorPlanImagePresignedUrlGenerator;

    @Mock
    private FloorPlanImageJsonCodec floorPlanImageJsonCodec;

    @Test
    @DisplayName("create()는 다중 이미지 기반 도면을 생성한다")
    void create_success() {
        AdminFloorPlanCreateRequest request = new AdminFloorPlanCreateRequest(
                "테스트 도면",
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.UNDER_5,
                "도면 프롬프트",
                List.of(
                        new AdminFloorPlanImageRequest("https://image/1", "fp-1.png", "room-1.png", "png", 1, "창가 뷰"),
                        new AdminFloorPlanImageRequest("https://image/2", "fp-2.png", "room-2.png", "png", 2, "복도 뷰")
                )
        );
        List<FloorPlanImageItem> images = List.of(
                new FloorPlanImageItem("https://image/1", "fp-1.png", "room-1.png", "png", 1, "창가 뷰"),
                new FloorPlanImageItem("https://image/2", "fp-2.png", "room-2.png", "png", 2, "복도 뷰")
        );
        FloorPlan floorPlan = FloorPlan.create(
                "테스트 도면",
                Form.OFFICETEL,
                Structure.OPEN_ONE_ROOM,
                Equilibrium.UNDER_5,
                "도면 프롬프트",
                or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImages.from(images),
                "[{\"url\":\"https://image/1\"}]"
        );
        ReflectionTestUtils.setField(floorPlan, "id", 1L);

        when(floorPlanImageJsonCodec.write(anyList())).thenReturn("[{\"url\":\"https://image/1\"}]");
        when(floorPlanRepository.saveAndFlush(any(FloorPlan.class))).thenReturn(floorPlan);
        when(floorPlanImageJsonCodec.read(anyString())).thenReturn(images);

        AdminFloorPlanResponse response = adminFloorPlanService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.floorPlanName()).isEqualTo("테스트 도면");
        assertThat(response.form()).isEqualTo(Form.OFFICETEL);
        assertThat(response.structure()).isEqualTo(Structure.OPEN_ONE_ROOM);
        assertThat(response.equilibrium()).isEqualTo(Equilibrium.UNDER_5);
        assertThat(response.images().getFirst().view()).isEqualTo("창가 뷰");
        assertThat(response.images()).hasSize(2);
        assertThat(response.representativeImageUrl()).isEqualTo("https://image/1");
        verify(floorPlanRepository).saveAndFlush(any(FloorPlan.class));
    }

    @Test
    @DisplayName("update()는 대표 이미지를 포함해 도면 정보를 수정한다")
    void update_success() {
        FloorPlan floorPlan = FloorPlan.create(
                "기존 도면",
                Form.VILLA,
                Structure.SEPARATED_ONE_ROOM,
                Equilibrium.BETWEEN_6_10,
                "기존 프롬프트",
                or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImages.from(List.of(
                        new FloorPlanImageItem("https://old-image", "old.png", "old-original.png", "png", 1, "기존 뷰")
                )),
                "[{\"url\":\"https://old-image\"}]"
        );
        ReflectionTestUtils.setField(floorPlan, "id", 2L);

        AdminFloorPlanUpdateRequest request = new AdminFloorPlanUpdateRequest(
                "수정 도면",
                Form.APARTMENT,
                Structure.TWO_ROOM,
                Equilibrium.BETWEEN_11_15,
                "수정 프롬프트",
                List.of(
                        new AdminFloorPlanImageRequest("https://new-image/1", "new-1.png", "new-room-1.png", "png", 1, "한강 뷰"),
                        new AdminFloorPlanImageRequest("https://new-image/2", "new-2.png", "new-room-2.png", "png", 2, "마운틴 뷰")
                )
        );
        List<FloorPlanImageItem> updatedImages = List.of(
                new FloorPlanImageItem("https://new-image/1", "new-1.png", "new-room-1.png", "png", 1, "한강 뷰"),
                new FloorPlanImageItem("https://new-image/2", "new-2.png", "new-room-2.png", "png", 2, "마운틴 뷰")
        );

        when(floorPlanRepository.findById(2L)).thenReturn(Optional.of(floorPlan));
        when(floorPlanImageJsonCodec.write(anyList())).thenReturn("[{\"url\":\"https://new-image/1\"}]");
        when(floorPlanRepository.saveAndFlush(floorPlan)).thenReturn(floorPlan);
        when(floorPlanImageJsonCodec.read(anyString())).thenReturn(updatedImages);

        AdminFloorPlanResponse response = adminFloorPlanService.update(2L, request);

        assertThat(response.floorPlanName()).isEqualTo("수정 도면");
        assertThat(response.form()).isEqualTo(Form.APARTMENT);
        assertThat(response.structure()).isEqualTo(Structure.TWO_ROOM);
        assertThat(response.equilibrium()).isEqualTo(Equilibrium.BETWEEN_11_15);
        assertThat(response.images().getFirst().view()).isEqualTo("한강 뷰");
        assertThat(response.representativeImageUrl()).isEqualTo("https://new-image/1");
        assertThat(response.images()).hasSize(2);
        assertThat(floorPlan.getUrl()).isEqualTo("https://new-image/1");
        assertThat(floorPlan.getFilename()).isEqualTo("new-1.png");
        assertThat(floorPlan.getOriginalFilename()).isEqualTo("new-room-1.png");
        assertThat(floorPlan.getFileExtension()).isEqualTo("png");
    }

    @Test
    @DisplayName("createImageUploadUrl()은 presigned URL을 반환한다")
    void createImageUploadUrl_success() {
        when(floorPlanImagePresignedUrlGenerator.createImageUploadUrl(any(AdminFloorPlanImageUploadRequest.class), eq("image/png")))
                .thenReturn(new AdminFloorPlanImageUploadResponse("https://upload-url", "https://public-url"));

        AdminFloorPlanImageUploadResponse response = adminFloorPlanService.createImageUploadUrl(
                new AdminFloorPlanImageUploadRequest("png"),
                "image/png"
        );

        assertThat(response.uploadUrl()).isEqualTo("https://upload-url");
        assertThat(response.publicUrl()).isEqualTo("https://public-url");
    }
}
