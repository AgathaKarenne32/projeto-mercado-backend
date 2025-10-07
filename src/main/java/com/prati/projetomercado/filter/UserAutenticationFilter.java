package com.prati.projetomercado.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.prati.projetomercado.config.SecurityConfiguration;
import com.prati.projetomercado.repository.AccessTokenRepository;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.service.impl.UserDetailsServiceImpl;
import com.prati.projetomercado.utils.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class UserAutenticationFilter extends OncePerRequestFilter {

    private final JwtTokenServiceImpl jwtTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AccessTokenRepository accessTokenRepository;
    @Qualifier("")
    private final HandlerMappingIntrospector introspector;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return Arrays.stream(SecurityConfiguration.PUBLIC_ENDPOINTS)
                .anyMatch(p -> new MvcRequestMatcher(introspector, p).matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            String token = TokenUtils.recoveryToken(request.getHeader("Authorization")); // <-- CORRETO

            if (token != null) {
                // 1. VERIFICAÇÃO NO BANCO: O token existe no banco de dados?
                // Se não existir, o .orElseThrow() vai lançar uma exceção e o 'catch' abaixo vai tratar.
                accessTokenRepository.findByToken(token)
                        .orElseThrow(() -> new JWTVerificationException("Token invalidado (logout) ou não encontrado no banco de dados."));

                // 2. VERIFICAÇÃO DO JWT: A assinatura e data do token são válidas?
                var email = jwtTokenService.getSubjectFromToken(token);

                // 3. AUTENTICAÇÃO: Carrega os detalhes do usuário e o autentica no sistema.
                var userDetails = userDetailsService.loadUserByUsername(email);
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Se qualquer verificação falhar, o erro é capturado aqui.
            logger.error("Falha no filtro de segurança: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido, expirado ou revogado.");
            return; // Interrompe o fluxo
        }
    }
}