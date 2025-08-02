package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.model.JwtToken;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthUserRepository userRepository;
    private UserService userService;

    public AuthController(AuthUserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody CreateUserRequest userRequest) {
        userService.registerUser(userRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody LoginUserRequest userRequest) throws Exception {
        System.out.println("Foi controller");
       var jwtToken = userService.login(userRequest);
       return new ResponseEntity<>(jwtToken, HttpStatus.OK);
       
    }


}
