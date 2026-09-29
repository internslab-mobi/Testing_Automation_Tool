package xyz.mobi.testingautomationtool.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import xyz.mobi.testingautomationtool.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Integer userId;
    private final String username;
    private final String email;
    private final String password;
    private final String fullName;
    private final String roleName;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.active = user.isActive();

        String rawRole = (user.getRole() != null && user.getRole().getRole() != null)
                ? user.getRole().getRole().toUpperCase().trim()
                : "TESTER";
        this.roleName = rawRole;

        List<GrantedAuthority> auths = new ArrayList<>();
        if (!rawRole.startsWith("ROLE_")) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + rawRole));
        } else {
            auths.add(new SimpleGrantedAuthority(rawRole));
        }
        auths.add(new SimpleGrantedAuthority(rawRole.replace("ROLE_", "")));

        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return active;
    }
}
