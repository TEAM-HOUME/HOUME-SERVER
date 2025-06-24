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

    // 소셜로그인이기에 username이 존재하지 않ㄹ음
    @Override
    public String getUsername() {
        return "";
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
