package com.example.spring.security.oauth2.config;

import com.example.spring.security.oauth2.controller.OAuth2ClientHandler;
import com.example.spring.security.oauth2.controller.PersonHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class Routes {


    private final ReactiveClientRegistrationRepository clientRegistrationRepository;
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    public Routes(ReactiveClientRegistrationRepository clientRegistrationRepository, ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Bean
    public RouterFunction<ServerResponse> loginRoute() {
        return route().path("/login", builder -> builder
                .GET((request) -> ServerResponse
                        .ok()
                        .render("oauth2-login")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> personRoute() {
        return route().path("/persons", builder -> builder
                        .GET((request) -> ServerResponse
                                .ok()
                                .render("persons-view",
                                        "persons", new PersonHandler().getAllPersons())))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> clientRoute() {
        return route().path("/oauth2", builder -> builder
                        .GET("/clients/{name}",
                                accept(APPLICATION_JSON),
                                new OAuth2ClientHandler(clientRegistrationRepository, authorizedClientService)::getClientRegistration))
                .build();
    }
}
