package com.example.spring.security.oauth2.config;

import com.example.spring.security.oauth2.controller.PersonController;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.parsing.Parser;
import lombok.SneakyThrows;
import net.jcip.annotations.NotThreadSafe;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;


@NotThreadSafe
@Import({SecurityConfig.class, TestClientConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SecurityConfigJWTE2ETest {

    private static KeycloakContainer keycloak;

    static {
        keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:22.0")
                .withRealmImportFile("realm-test-spring.json")
                .withDisabledCaching()
                .withEnabledMetrics()
                .withReuse(false);

        keycloak.start();
    }
    @LocalServerPort
    private int port;

    @Autowired
    private PersonController controller;

    @Autowired @Qualifier("testNoCertVerificationRestTemplate")
    private RestTemplate restTemplate;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        RestAssured.registerParser("text/plain", Parser.HTML);
        RestAssured.useRelaxedHTTPSValidation();
        RestAssuredMockMvc.standaloneSetup(controller);
    }

    @DynamicPropertySource
    static void updateKeycloakConfiguration(DynamicPropertyRegistry registry) {

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/test-spring");

        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/test-spring/protocol/openid-connect/certs");
    }


    @Test
    @DisplayName("Should generate token for password grant type ")
    public void testReturnTokenForPasswordGrantType() throws Exception {

        URI authorizationURI = new URIBuilder(keycloak.getAuthServerUrl() + "/realms/test-spring/protocol/openid-connect/token").build();

        MultiValueMap<String, String> formData = getKeyCloakLoginDetails();

        var result = restTemplate.postForEntity(authorizationURI, formData, String.class);

        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());

    }

    @Test
    @DisplayName("Should accept JTW bearer token ")
    public void testAcceptJWTBearerToken() throws Exception {

        URI authorizationURI = new URIBuilder(keycloak.getAuthServerUrl() + "/realms/test-spring/protocol/openid-connect/token").build();

        MultiValueMap<String, String> formData = getKeyCloakLoginDetails();

        var result = restTemplate.postForEntity(authorizationURI, formData, String.class);

        String bearerToken = asBearerToken(result);


        given().header("Authorization", bearerToken)
                .when()
                .get("https://localhost:"+port+"/persons")
                .then()
                .body(containsString("Andy"))
                .statusCode(200);

    }

    @NotNull
    private static MultiValueMap<String, String> getKeyCloakLoginDetails() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.put("grant_type", Collections.singletonList("password"));
        formData.put("client_id", Collections.singletonList("test-spring-client"));
        formData.put("username", Collections.singletonList("test"));
        formData.put("password", Collections.singletonList("test"));
        return formData;
    }

    @NotNull
    private static String asBearerToken(ResponseEntity<String> result) {
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        var bearerToken = "Bearer " + jsonParser.parseMap(result.getBody())
                .get("access_token")
                .toString();
        return bearerToken;
    }

}
