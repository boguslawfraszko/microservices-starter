package com.example.spring.security.oauth2.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/oauth2")
@Slf4j
public class OAuth2ClientController {
    @Autowired
    private ReactiveClientRegistrationRepository clientRegistrationRepository;
    @Autowired
    private ReactiveOAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/clients/{name}")
    public Mono<ClientRegistration> getClientRegistration(@PathVariable("name") String clientRegistrationId) {
        return this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
    }

    @GetMapping("/access-token/{name}")
    public Mono<OAuth2AccessToken> getAccessToken(Authentication authentication, @PathVariable("name") String clientRegistrationId) {
        log.info("authentication "+ authentication);
        return this.authorizedClientService.
                loadAuthorizedClient(clientRegistrationId, authentication.getName())
                .map(client -> client.getAccessToken());
    }

    @GetMapping("/oidc-principal")
    public Mono<OidcUser> getOidcUserPrincipal(@AuthenticationPrincipal OidcUser principal) {
        return Mono.just(principal);
    }

    @GetMapping("/oauth-principal")
    public Mono<OAuth2User> getOAuthUserPrincipal(@AuthenticationPrincipal OAuth2User principal) {
        return Mono.just(principal);
    }
}
