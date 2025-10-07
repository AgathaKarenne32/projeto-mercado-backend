package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.dto.response.UserResponse; // adicionado para a issues 9
import com.prati.projetomercado.dto.request.ChangePasswordRequest; // adicionado para a issues 9
import com.prati.projetomercado.model.JwtToken;
import org.springframework.stereotype.Service;
import com.prati.projetomercado.entity.AuthUser;


import java.util.UUID;

@Service
public interface UserService {
    
    public void registerUser(CreateUserRequest loginUserRequest);
    
    public JwtToken login(LoginUserRequest loginUserRequest) throws Exception;

    public JwtToken useRefreshToken(String accessToken, UUID refreshTokenId);

    AuthUser registerOAuth2User(String username, String email);

    /**
     * Busca as informações do usuário atualmente autenticado.
     * @return Um DTO com os dados públicos do usuário.
     */
    UserResponse getUserInfo();

    /**
     * Altera a senha do usuário autenticado.
     * @param request DTO contendo a senha atual, a nova senha e a confirmação.
     */
    void changePassword(ChangePasswordRequest request);

    void confirmUser(String token);
}
