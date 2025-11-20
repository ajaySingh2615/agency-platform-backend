package com.createrapp.backend.security;

import com.createrapp.backend.entity.User;
import com.createrapp.backend.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private UUID userId;
    private String email;
    private String phoneNumber;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isEnabled;

    /**
     * Create from User entity
     */
    public static CustomUserDetails create(User user) {
        Set<GrantedAuthority> authorities = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName().name()))
                .collect(Collectors.toSet());

        boolean isEnabled = !user.getAccountStatus().toString().equals("BANNED");

        return new CustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getPasswordHash(),
                authorities,
                isEnabled
        );
    }

    @Override
    public String getUsername() {
        return email != null ? email : phoneNumber;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return isEnabled;
    }
}
