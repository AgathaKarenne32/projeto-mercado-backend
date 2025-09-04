package com.prati.projetomercado.config;

import com.prati.projetomercado.filter.UserAutenticationFilter;
import com.prati.projetomercado.security.oauth2.CustomOAuth2UserService;
import com.prati.projetomercado.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.prati.projetomercado.security.oauth2.handlers.OAuth2AuthSuccessHandler;
import com.prati.projetomercado.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/register",
            "/auth/register/",
            "/auth/login",
            "/auth/login/",
            "/auth/refresh-token",
            "/auth/refresh-token/",
            "/h2-console/**",
            "/h2-console/",
            "/h2-console",
            "/oauth2/**"
    };

    public static final String[] AUTH_REQUIRED_ENDPOINTS = {
            "/auth/test-autenticated",
    };

    @Autowired
    private UserAutenticationFilter userAutenticationFilter;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired private OAuth2AuthSuccessHandler oAuth2AuthSuccessHandler;

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }


    @Bean
    @Order(10)
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource((CorsConfigurationSource) urlBasedCorsConfigurationSource()))
                .headers(HeadersConfigurer::disable)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                .anyRequest().permitAll()
                ).addFilterBefore(userAutenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(
                        configurer ->
                                configurer.authorizationEndpoint(
                                        endpoint -> endpoint
                                                .baseUri("/oauth2/authorize")
                                                .authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository())

                                )
                                        .redirectionEndpoint(endpoint -> endpoint
                                                .baseUri("/oauth2/callback/*"))
                                        .userInfoEndpoint(endpoint -> endpoint
                                                .userService(customOAuth2UserService))
                                        .successHandler(oAuth2AuthSuccessHandler)
                )
                .build();

    }

    UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://127.0.0.1:5173", "http://localhost:5173", "*"));
        corsConfiguration.setAllowedMethods(List.of("GET","POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;

    }

    @Bean
    public FilterRegistrationBean<UserAutenticationFilter> userAutenticationFilterFilterRegistrationBean() {
        var filter = new FilterRegistrationBean<UserAutenticationFilter>();
        filter.setFilter(userAutenticationFilter);
        filter.addUrlPatterns(AUTH_REQUIRED_ENDPOINTS);
        return filter;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder
    ) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

}
