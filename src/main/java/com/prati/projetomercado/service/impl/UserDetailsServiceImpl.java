package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.repository.AuthUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private AuthUserRepository authUserRepository;
    public UserDetailsServiceImpl(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       var authUser = authUserRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found")); 
       return new UserDetailsImpl(authUser);
    }
    
}
