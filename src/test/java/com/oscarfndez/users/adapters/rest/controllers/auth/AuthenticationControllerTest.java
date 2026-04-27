package com.oscarfndez.users.adapters.rest.controllers.auth;

import com.oscarfndez.users.adapters.rest.dtos.auth.JwtAuthenticationResponse;
import com.oscarfndez.users.adapters.rest.dtos.auth.SignUpRequest;
import com.oscarfndez.users.adapters.rest.dtos.auth.SigninRequest;
import com.oscarfndez.users.core.services.auth.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    void signupReturnsJwtFromService() {
        SignUpRequest request = SignUpRequest.builder()
                .email("oscar@example.com")
                .password("raw-password")
                .build();
        JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
                .token("jwt-token")
                .build();
        when(authenticationService.signup(request)).thenReturn(jwtResponse);

        var response = authenticationController.signup(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(jwtResponse);
        verify(authenticationService).signup(request);
    }

    @Test
    void signinReturnsJwtFromService() {
        SigninRequest request = SigninRequest.builder()
                .email("oscar@example.com")
                .password("raw-password")
                .build();
        JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
                .token("jwt-token")
                .build();
        when(authenticationService.signin(request)).thenReturn(jwtResponse);

        var response = authenticationController.signin(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(jwtResponse);
        verify(authenticationService).signin(request);
    }
}
