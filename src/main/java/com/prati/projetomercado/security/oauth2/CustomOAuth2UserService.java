package com.prati.projetomercado.security.oauth2;

import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.security.oauth2.user.OAuth2UserInfo;
import com.prati.projetomercado.security.oauth2.user.OAuth2UserInfoFactory;
import com.prati.projetomercado.service.impl.UserServiceImpl;
import com.sun.security.auth.UserPrincipal;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.prati.projetomercado.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;


import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomOAuth2UserService extends DefaultOAuth2UserService {


    private final AuthUserRepository authUserRepository;

    private final ObjectProvider<UserService> userServiceProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }

    }

    public OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        var oauth2Userinfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (StringUtils.isEmpty(oauth2Userinfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email is empty from OAuth2 provider");
        }

        Optional<AuthUser> authUserOptional = authUserRepository.findByEmail(oauth2Userinfo.getEmail());

        AuthUser authUser;
        authUser = authUserOptional.orElseGet(() -> registerNewUser(oauth2Userinfo));

        return UserDetailsImpl.build(authUser) ;
    }

    private AuthUser registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        //Pegamos a instância do UserService do "fornecedor" apenas quando o metodo é chamado.
        UserService userService = userServiceProvider.getObject();

        return userService.registerOAuth2User(
                oAuth2UserInfo.getName(),
                oAuth2UserInfo.getEmail()
        );
    }
}
