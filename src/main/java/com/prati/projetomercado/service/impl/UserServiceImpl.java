package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.config.SecurityConfiguration;
import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.model.JwtToken;
import com.prati.projetomercado.repository.AuthUser;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private AuthenticationManager authenticationManager;
    private AuthUserRepository userRepository;
    private JwtTokenServiceImpl jwtTokenService;
    private PasswordEncoder encoder;

    public UserServiceImpl(AuthenticationManager authenticationManager, AuthUserRepository userRepository, JwtTokenServiceImpl jwtTokenService, PasswordEncoder encoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.encoder = encoder;
    }

    @Override
    public void registerUser(CreateUserRequest createUserRequest) {
        var newUser = AuthUser.builder()
                .email(createUserRequest.email())
                .password(encoder.encode(createUserRequest.password()))
                .build();
        userRepository.save(newUser);
    }

    @Override
    public JwtToken login(LoginUserRequest loginUserRequest) throws Exception {
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginUserRequest.email(), loginUserRequest.password());
        Authentication authentication;

        try {
            System.out.println(usernamePasswordAuthenticationToken.toString());
            authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (Exception e) {
            throw new Exception("Authmanager error", e);
        }

        var userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();

        return new JwtToken(jwtTokenService.generateToken(userDetailsImpl));
    }
}
