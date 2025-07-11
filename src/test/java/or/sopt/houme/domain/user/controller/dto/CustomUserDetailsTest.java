package or.sopt.houme.domain.user.controller.dto;

import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    @DisplayName("정상적인 user 객체로 CustomUserDetails를 생성하고 메서드 값을 검증한다")
    void customUserDetails_normalUser() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // when & then
        assertEquals("test@example.com", userDetails.getEmail());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertEquals("", userDetails.getUsername());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    @DisplayName("user가 null이면 getAuthorities() 호출 시 USER_NOT_FOUND 예외가 발생한다")
    void customUserDetails_nullUser() {
        CustomUserDetails userDetails = new CustomUserDetails(null);

        UserException exception = assertThrows(UserException.class, userDetails::getAuthorities);
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("user의 role이 null이면 getAuthorities() 호출 시 USER_ROLE_EXCEPTION 예외가 발생한다")
    void customUserDetails_nullRole() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .role(null)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UserException exception = assertThrows(UserException.class, userDetails::getAuthorities);
        assertEquals(ErrorCode.USER_ROLE_EXCEPTION, exception.getErrorCode());
    }
}
