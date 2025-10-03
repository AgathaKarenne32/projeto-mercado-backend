package com.prati.projetomercado.security.oauth2.handlers;

import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.entity.AccessToken;
import com.prati.projetomercado.entity.RefreshToken;
import com.prati.projetomercado.repository.AccessTokenRepository;
import com.prati.projetomercado.repository.RefreshTokenRepository;
import com.prati.projetomercado.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class OAuth2AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenServiceImpl tokenService;
    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var targetURL = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.warn("Response has already been committed. Unable to redirect to " + targetURL);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetURL);



    }

    @SneakyThrows
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException("Unauthorized redirect URI");
        }

        String targetUri = redirectUri.orElse(getDefaultTargetUrl());

        var authUser = ((UserDetailsImpl) authentication.getPrincipal()).getAuthUser();
        var expiredDate = Instant.now().plus(1, ChronoUnit.DAYS);
        String accessToken = tokenService.generateToken(authUser, expiredDate);
        var accessTokenEntity = AccessToken.builder()
                .token(accessToken)
                .authUser(authUser)
                .expiredDate(expiredDate)
                .build();

        accessTokenRepository.save(accessTokenEntity);

        RefreshToken refreshToken= tokenService.generateNewRefreshToken(authUser);
        refreshTokenRepository.save(refreshToken);


        return UriComponentsBuilder.fromUriString(targetUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken.getId())
                .build().toUriString();

    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequests(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return true;
    }
}
