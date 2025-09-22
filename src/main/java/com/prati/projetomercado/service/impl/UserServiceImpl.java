package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
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
import com.prati.projetomercado.dto.response.UserResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.prati.projetomercado.dto.request.ChangePasswordRequest;


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

        if (createUserRequest.password().length() < 6) throw new BadCredentialsException(List.of(new FieldError("password", "Min length: 6 characters")));

        var newUser = AuthUser.builder().email(createUserRequest.email()).username(createUserRequest.username()).password(encoder.encode(createUserRequest.password())).build();
        userRepository.save(newUser);
    }

    @Override
    public JwtToken login(LoginUserRequest loginUserRequest) throws Exception {
        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginUserRequest.email(), loginUserRequest.password());
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (Exception e) {
            throw new AuthException("Auth manager error");
        }

        var userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();

        var accessTokenEntityOld = accessTokenRepository.findByAuthUser(userDetailsImpl.getAuthUser());

        if (accessTokenEntityOld != null) {
            accessTokenRepository.delete(accessTokenEntityOld);
        }

        var refreshToken = jwtTokenService.generateNewRefreshToken(userDetailsImpl.getAuthUser());

        refreshTokenRepository.save(refreshToken);
        var accessTokenExpirationDate = jwtTokenService.expirationAccessTokenDate();
        var accessToken = jwtTokenService.generateToken(userDetailsImpl.getAuthUser(), accessTokenExpirationDate);
        var accessTokenEntity = AccessToken.builder().authUser(userDetailsImpl.getAuthUser()).token(accessToken).expiredDate(accessTokenExpirationDate).build();
        accessTokenRepository.save(accessTokenEntity);

        return new JwtToken(accessToken, refreshToken.getId());
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

        var newRefreshToken = jwtTokenService.generateNewRefreshToken(authuser);
        refreshTokenRepository.save(newRefreshToken);
        var newAccessTokenExpDate = jwtTokenService.expirationAccessTokenDate();
        var newAccessToken = jwtTokenService.generateToken(authuser, newAccessTokenExpDate);

        var accessTokenEntityOld = accessTokenRepository.findByAuthUser(authuser);

        if (accessTokenEntityOld != null) {
            accessTokenRepository.delete(accessTokenEntityOld);
        }

        var newAccessTokenEntity = AccessToken.builder().authUser(authuser).token(newAccessToken).expiredDate(newAccessTokenExpDate).build();

        accessTokenRepository.save(newAccessTokenEntity);

        return new JwtToken(newAccessToken, newRefreshToken.getId());

    }

    @Override
    public UserResponse getUserInfo() {
        // 1. Pega o email do usuário a partir do token de segurança
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Busca o usuário completo no banco de dados usando o email
        AuthUser authUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        // 3. Converte a entidade AuthUser para o nosso DTO de resposta seguro
        return new UserResponse(
                authUser.getId(),
                authUser.getUsername(),
                authUser.getEmail(),
                authUser.getCreationDate()
        );
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        // 1. Pega o email do usuário a partir do token de segurança
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AuthUser currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        // 2. Verifica se a "senha atual" fornecida bate com a senha salva no banco.
        // O passwordEncoder.matches() compara a senha em texto plano com a senha criptografada.
        if (!encoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException(List.of(new FieldError("currentPassword", "A senha atual está incorreta."))); // TODO: Criar exceção customizada se preferir
        }

        // 3. Verifica se a "nova senha" e a "confirmação" são iguais.
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BadCredentialsException(List.of(new FieldError("confirmNewPassword", "A nova senha e a confirmação não conferem.")));
        }

        // 4. (Opcional, mas recomendado) Adicionar validações para a nova senha.
        if (request.newPassword().length() < 8) {
            throw new BadCredentialsException(List.of(new FieldError("newPassword", "A nova senha deve ter no mínimo 8 caracteres.")));
        }

        // 5. Se todas as verificações passaram, criptografa e atualiza a senha.
        currentUser.setPassword(encoder.encode(request.newPassword()));

        // 6. Salva o usuário com a nova senha no banco de dados.
        userRepository.save(currentUser);
    }
}
