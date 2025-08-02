package com.prati.projetomercado.config;

import com.prati.projetomercado.repository.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {
    
    private AuthUser authUser;

    public UserDetailsImpl(AuthUser authUser) {
        this.authUser = authUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return authUser.getEmail();
    }
    
    @Override
    public String getPassword() {
        return authUser.getPassword(); 
    }
    
}
