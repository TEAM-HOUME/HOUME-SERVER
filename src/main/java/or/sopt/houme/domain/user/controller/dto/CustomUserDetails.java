package or.sopt.houme.domain.user.controller.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    /**
     * 여기에 회원 정보를 저장해서 우리가 회원정보를 가져올 수 있게 됩니다!
     *
     * 해당 세션의 생명주기는 하나의 요청에 한정됩니다
     * 새로운 요청이 들어오면 UserDetail 은 초기화 됩니다
     * */
    private final User user;

    // Role 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> {
            if (user != null) {
                return user.getRole().name();
            }
            return null;
        });
        return collection;
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 소셜로그인이기에 username이 존재하지 않음
    @Override
    public String getUsername() {
        return "";
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
