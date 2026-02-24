package or.sopt.houme.domain.user.presentation.controller.dto;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        return customUserDetails;
    }

    // JWT 토큰에서 ID 기반으로 조회할 때 사용하는 메서드
    public CustomUserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        return new CustomUserDetails(user);
    }
}
