package com.prati.projetomercado.config;

import com.prati.projetomercado.entity.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails, OAuth2User {

    private AuthUser authUser;

    public UserDetailsImpl(AuthUser authUser) {
        this.authUser = authUser;
    }

    public static UserDetailsImpl build(AuthUser authUser) {
        return new UserDetailsImpl(authUser);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getUsername() {
        return authUser.getEmail();
    }

    @Override
    public String getPassword() {
        return authUser.getPassword();
    }

    public AuthUser getAuthUser() { return authUser;}

    @Override
    public String getName() {
        return "";
    }
}
