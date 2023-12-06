package com.example.spring.security.oauth2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class SecurityConfig {
    @Autowired
    private OAuth2ClientProperties oAuth2ClientProperties;

    @Autowired
    private Environment env;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/login**",
                                "/logout**",
                                "/error",
                                "/webjars/**",
                                "/templates/**",
                                "/css/**.css",
                                "/actuator/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .requiresLogout(new PathPatternParserServerWebExchangeMatcher("/logout"))
                )
                .headers(headers -> {
                    headers.frameOptions().mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN);
                    headers.cache().disable();
                    headers.hsts(hsts -> hsts.includeSubdomains(true)
                            .preload(true)
                            .maxAge(Duration.ofDays(365)) // Equivalent to 31536000 seconds
                    );
                })
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://localhost:8443"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties oAuth2ClientProperties) {
        List<ClientRegistration> registrations = oAuth2ClientProperties.getRegistration().keySet().stream()
                .map(client -> getRegistration(client, oAuth2ClientProperties))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new InMemoryReactiveClientRegistrationRepository(registrations);
    }

    @Bean
    public ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    private ClientRegistration getRegistration(String client, OAuth2ClientProperties oAuth2ClientProperties) {
        OAuth2ClientProperties.Registration registration = oAuth2ClientProperties.getRegistration().get(client);
        String clientId = registration.getClientId();
        if (clientId == null) {
            return null;
        }

        String clientSecret = registration.getClientSecret();
        return switch (client) {
            case "google" -> CommonOAuth2Provider.GOOGLE.getBuilder(client)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
            case "github" -> CommonOAuth2Provider.GITHUB.getBuilder(client)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
            case "facebook" -> CommonOAuth2Provider.FACEBOOK.getBuilder(client)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .scope("public_profile")
                    .build();
            default -> {
                OAuth2ClientProperties.Provider provider = this.oAuth2ClientProperties.getProvider().get(client);
                yield ClientRegistration.withRegistrationId(client)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                        .scope("openid")
                        .authorizationUri(provider.getAuthorizationUri())
                        .tokenUri(provider.getTokenUri())
                        .userInfoUri(provider.getUserInfoUri())
                        .jwkSetUri(provider.getJwkSetUri())
                        .userNameAttributeName(IdTokenClaimNames.SUB)
                        .clientName(client)
                        .build();
            }
        };
    }

}

