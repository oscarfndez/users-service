package com.oscarfndez.users.integration;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.adapters.rest.dtos.WhoAmIDto;
import com.oscarfndez.users.adapters.rest.dtos.auth.JwtAuthenticationResponse;
import com.oscarfndez.users.adapters.rest.dtos.auth.SignUpRequest;
import com.oscarfndez.users.adapters.rest.dtos.auth.SigninRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationFlowIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void signupCreatesUserInPostgresAndReturnsJwt() {
        SignUpRequest request = SignUpRequest.builder()
                .firstName("Oscar")
                .lastName("Fernandez")
                .email("signup-it@example.com")
                .password("password")
                .build();

        var response = restTemplate.postForEntity(
                "/api/v1/auth/signup",
                request,
                JwtAuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        var savedUser = userRepository.findByEmail("signup-it@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void signinWithUserCreatedByCurrentTestReturnsJwt() {
        userRepository.save(User.builder()
                .firstName("Oscar")
                .lastName("Fernandez")
                .email("signin-it@example.com")
                .password("password")
                .role(Role.USER)
                .build());
        SigninRequest request = SigninRequest.builder()
                .email("signin-it@example.com")
                .password("password")
                .build();

        var response = restTemplate.postForEntity(
                "/api/v1/auth/signin",
                request,
                JwtAuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    void whoAmIReturnsUserFromJwtCreatedByCurrentTest() {
        String token = signupAndReturnToken("whoami-it@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        var response = restTemplate.postForEntity(
                "/api/v1/auth/signin",
                SigninRequest.builder()
                        .email("whoami-it@example.com")
                        .password("password")
                        .build(),
                JwtAuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        var whoAmIResponse = restTemplate.exchange(
                "/api/whoami",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                WhoAmIDto.class);

        assertThat(whoAmIResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(whoAmIResponse.getBody()).isNotNull();
        assertThat(whoAmIResponse.getBody().getEmail()).isEqualTo("whoami-it@example.com");
        assertThat(whoAmIResponse.getBody().getRole()).isEqualTo("USER");
    }

    private String signupAndReturnToken(String email) {
        SignUpRequest request = SignUpRequest.builder()
                .firstName("Oscar")
                .lastName("Fernandez")
                .email(email)
                .password("password")
                .build();

        var response = restTemplate.postForEntity(
                "/api/v1/auth/signup",
                request,
                JwtAuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getToken();
    }
}
