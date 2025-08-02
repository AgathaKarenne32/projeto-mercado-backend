package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.model.JwtToken;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    
    public void registerUser(CreateUserRequest loginUserRequest);
    
    public JwtToken login(LoginUserRequest loginUserRequest) throws Exception;
}
