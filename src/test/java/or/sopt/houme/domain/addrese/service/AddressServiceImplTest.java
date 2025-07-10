package or.sopt.houme.domain.addrese.service;

import or.sopt.houme.domain.addrese.dto.request.AddressRequest;
import or.sopt.houme.domain.addrese.entity.Address;
import or.sopt.houme.domain.addrese.repository.AddressRepository;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("[AddressService] Test")
class AddressServiceImplTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자는 주소를 등록 할 수 있다.")
    void createAddress() {
        // Given
        User user = User.builder()
                .name("test_user")
                .birthday(LocalDate.of(2001, 1, 10))
                .gender(Gender.MALE)
                .email("example.com")
                .password(null)
                .hasGeneratedImage(false)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        String sigungu = "서울특별시 솝트구";
        String roadName = "하우미로 123";

        AddressRequest addressRequest = new AddressRequest(sigungu, roadName);

        // When
        addressService.createAddress(user, addressRequest);

        // Then
        Optional<Address> byId = addressRepository.findById(1L);

        assertThat(byId).isPresent();
        assertThat(byId.get().getId()).isEqualTo(1L);
        assertThat(byId.get().getSigungu()).isEqualTo(sigungu);
        assertThat(byId.get().getRoadName()).isEqualTo(roadName);
    }
}