package com.prati.projetomercado.controller;


import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.dto.request.RefreshTokenRequest;
import com.prati.projetomercado.dto.response.ErrorResponse;
import com.prati.projetomercado.exceptions.AuthException;
import com.prati.projetomercado.model.JwtToken;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.service.UserService;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenServiceImpl jwtTokenServiceImpl;
    private AuthUserRepository userRepository;
    private UserService userService;
    private JwtTokenServiceImpl tokenService;

    public AuthController(AuthUserRepository userRepository, UserService userService, JwtTokenServiceImpl tokenService, JwtTokenServiceImpl jwtTokenServiceImpl) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.tokenService = tokenService;
        this.jwtTokenServiceImpl = jwtTokenServiceImpl;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody CreateUserRequest userRequest) {
        String username = userRequest.username();
        String password = userRequest.password();
        String confirmPassword = userRequest.confirmPassword();

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("as senhas não conferem"));
        }

        if (password.length() < 8) {
            return ResponseEntity.badRequest().body(new ErrorResponse("as senhas devem ter no mínimo 8 caracteres"));
        }

        userService.registerUser(userRequest);

        return ResponseEntity.ok("Usuário cadastrado com sucesso!");
    }


    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody LoginUserRequest userRequest) throws Exception {
       var jwtToken = userService.login(userRequest);
       return new ResponseEntity<>(jwtToken, HttpStatus.OK);
       
    }

    //adicionando logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorization) {
        String token = TokenUtils.recoveryToken(authorization);
        jwtTokenServiceImpl.invalidateToken(token); // você precisa implementar isso
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }



    @PostMapping("/refresh-token")
    public ResponseEntity<JwtToken> refresh(@RequestHeader String Authorization, @RequestBody RefreshTokenRequest refreshToken) throws Exception {
        var newJwtToken = userService.useRefreshToken(TokenUtils.recoveryToken(Authorization), UUID.fromString(refreshToken.refreshToken()));
        return new ResponseEntity<>(newJwtToken, HttpStatus.OK);
    }

    @PostMapping("/test-autenticated")
    public ResponseEntity<String> test(@RequestHeader String Authorization, @RequestBody String alow) throws Exception {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }


    @GetMapping("/confirm-registration")
    @Operation(summary = "Confirma o registro de um novo usuário", description = "Endpoint ativado pelo link enviado ao e-mail do usuário para validar a conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token) {
        try {
            userService.confirmUser(token);
            return ResponseEntity.ok("Conta ativada com sucesso! Você já pode fazer o login.");
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
