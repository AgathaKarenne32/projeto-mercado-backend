package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.dto.response.AuthResponse;
import com.prati.projetomercado.dto.response.UserResponse;
import com.prati.projetomercado.entity.AccessToken;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.exceptions.BadCredentialsException;
import com.prati.projetomercado.exceptions.FieldError;
import com.prati.projetomercado.model.JwtToken;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.repository.AccessTokenRepository;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.RefreshTokenRepository;
import com.prati.projetomercado.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private AuthenticationManager authenticationManager;
    private AuthUserRepository userRepository;
    private JwtTokenServiceImpl jwtTokenService;
    private PasswordEncoder encoder;
    private RefreshTokenRepository refreshTokenRepository;
    private AccessTokenRepository accessTokenRepository;

    public UserServiceImpl(AuthenticationManager authenticationManager, AuthUserRepository userRepository, JwtTokenServiceImpl jwtTokenService, PasswordEncoder encoder, RefreshTokenRepository refreshTokenRepository, AccessTokenRepository accessTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.encoder = encoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    public void registerUser(CreateUserRequest createUserRequest) {
        if (!createUserRequest.password().equals(createUserRequest.confirmPassword())) {
            throw new BadCredentialsException(
                    List.of(new FieldError("confirmPassword", "Passwords don't match"), new FieldError("password", "Passwords don't match")));
        }

        if (createUserRequest.password().length() < 6)
            throw new BadCredentialsException(List.of(new FieldError("password", "Min length: 6 characters")));

        var newUser = AuthUser.builder().email(createUserRequest.email()).username(createUserRequest.username()).password(encoder.encode(createUserRequest.password())).build();
        userRepository.save(newUser);
    }

    @Override
    public AuthResponse login(LoginUserRequest loginUserRequest) throws Exception {
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginUserRequest.email(), loginUserRequest.password());
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (Exception e) {
            throw new AuthException("Auth manager error");
        }

        var userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();

        var refreshToken = jwtTokenService.generateNewRefreshToken(userDetailsImpl.getAuthUser());

        refreshTokenRepository.save(refreshToken);
        var accessTokenExpirationDate = jwtTokenService.expirationAccessTokenDate();
        var accessToken = jwtTokenService.generateToken(userDetailsImpl.getAuthUser(), accessTokenExpirationDate);
        var accessTokenEntity = AccessToken.builder().authUser(userDetailsImpl.getAuthUser()).token(accessToken).expiredDate(accessTokenExpirationDate).build();
        accessTokenRepository.save(accessTokenEntity);

        var username = userDetailsImpl.getAuthUser().getUsername();
        var email = userDetailsImpl.getAuthUser().getEmail();

        return new AuthResponse(accessToken, refreshToken.getId(), new UserResponse(username, email));
    }

    private Optional<AuthUser> getAuthUser(String accessToken) {
        var email = jwtTokenService.getSubjectFromToken(accessToken);
        return userRepository.findByEmail(email);


    }

    @Override
    public JwtToken useRefreshToken(String accessToken, UUID refreshTokenId) {
        final var refreshToken = refreshTokenRepository.findByIdAndExpiresAtAfter(refreshTokenId, Instant.now()).orElseThrow(() -> new AuthException("No refreshToken found / refreshToken expired"));

        if (refreshToken.isAlreadyUsed()) {
            throw new AuthException("Token has already been used");
        }

        refreshToken.setAlreadyUsed(true);
        refreshTokenRepository.save(refreshToken);

        var authuser = getAuthUser(accessToken).orElseThrow(() -> new AuthException("user not found"));
        var accessTokenEntityOld = accessTokenRepository.findByAuthUserAndToken(authuser, accessToken);
        accessTokenEntityOld.ifPresent(accessTokenEntity -> {
            accessTokenRepository.delete(accessTokenEntity);
        });

        var newRefreshToken = jwtTokenService.generateNewRefreshToken(authuser);

        refreshTokenRepository.save(newRefreshToken);
        var newAccessTokenExpDate = jwtTokenService.expirationAccessTokenDate();
        var newAccessToken = jwtTokenService.generateToken(authuser, newAccessTokenExpDate);

        var newAccessTokenEntity = AccessToken.builder().authUser(authuser).token(newAccessToken).expiredDate(newAccessTokenExpDate).build();

        accessTokenRepository.save(newAccessTokenEntity);

        return new JwtToken(newAccessToken, newRefreshToken.getId());

    }
}
