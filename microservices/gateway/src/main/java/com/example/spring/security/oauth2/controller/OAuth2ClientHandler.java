package com.example.spring.security.oauth2.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Slf4j
public final class OAuth2ClientHandler {
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    public OAuth2ClientHandler(ReactiveClientRegistrationRepository clientRegistrationRepository, ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientService = authorizedClientService;
    }

    public Mono<ServerResponse> getClientRegistration(ServerRequest request) {
        String clientRegistrationId = request.pathVariables().get("name");
        return ServerResponse
                .ok()
                .bodyValue(this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId));
    }

    public Mono<OAuth2AccessToken> getAccessToken(Authentication authentication, ServerRequest request) {
        log.info("authentication "+ authentication);

        String clientRegistrationId = request.pathVariables().get("name");
        return this.authorizedClientService.
                loadAuthorizedClient(clientRegistrationId, authentication.getName())
                .map(client -> client.getAccessToken());
    }

    public Mono<OidcUser> getOidcUserPrincipal(@AuthenticationPrincipal OidcUser principal) {
        return Mono.just(principal);
    }

    public Mono<OAuth2User> getOAuthUserPrincipal(@AuthenticationPrincipal OAuth2User principal) {
        return Mono.just(principal);
    }
}
