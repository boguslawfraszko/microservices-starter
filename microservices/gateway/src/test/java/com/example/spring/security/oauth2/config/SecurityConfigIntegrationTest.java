package com.example.spring.security.oauth2.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;



@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import(SecurityConfig.class)
public class SecurityConfigIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setup() {

        webTestClient = WebTestClient.bindToServer()
                .build();
    }

    @Test
    @DisplayName("Test filter chain when requesting a protected page, then redirect to the login page")
    public void testFilterChainWhenRequestToProtectedPageThenRedirectToLoginPage() {
        webTestClient
                .get()
                .uri("/persons")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("**/login");
    }

    @Test
    @DisplayName("Test filter chain when requesting the login page, then return the login page")
    public void testFilterChainWhenRequestToLoginPageThenReturnLoginPage() {
        webTestClient
                .get()
                .uri("/login")
                .exchange()
                .expectStatus().isOk();
    }
    @Test
    @DisplayName("Test filter chain when requesting the logout page, then redirect to the default success URL")
    public void testFilterChainWhenRequestToLogoutPageThenRedirectToDefaultSuccessUrl() {

        webTestClient
                .get()
                .uri("/logout")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("**/login?logout");
    }
}
