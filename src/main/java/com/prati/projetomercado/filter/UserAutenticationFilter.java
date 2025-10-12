package com.prati.projetomercado.filter;

import com.prati.projetomercado.advice.ExceptionAdvice;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class UserAutenticationFilter extends OncePerRequestFilter {

    private final JwtTokenServiceImpl jwtTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AccessTokenRepository accessTokenRepository;
    private ExceptionAdvice advice;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return Arrays.stream(SecurityConfiguration.PUBLIC_ENDPOINTS).anyMatch(
                stringURI -> PathPatternRequestMatcher.withDefaults().matcher(stringURI).matches(request)
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            String token = TokenUtils.recoveryToken(request.getHeader("Authorization"));

            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token nao encontrado no header");
            }

            var accessToken = accessTokenRepository.findByToken(token)
                    .orElse(null);

            if (accessToken == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token não encontrado no banco de dados");
            }

            assert accessToken != null;
            if (accessToken.getExpiredDate().isBefore(Instant.now())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado");

            }
            var email = jwtTokenService.getSubjectFromToken(token);

            var userDetails = userDetailsService.loadUserByUsername(email);
            var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Falha no filtro de segurança: ", e);
            advice.handleException(e);
        }
    }

}
