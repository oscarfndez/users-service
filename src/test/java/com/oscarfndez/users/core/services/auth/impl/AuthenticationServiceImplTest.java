package com.oscarfndez.users.core.services.auth.impl;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.adapters.rest.dtos.auth.SignUpRequest;
import com.oscarfndez.users.adapters.rest.dtos.auth.SigninRequest;
import com.oscarfndez.users.core.services.auth.JwtService;
import com.oscarfndez.users.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void signupCreatesUserWithEncodedPasswordAndUserRole() {
        SignUpRequest request = SignUpRequest.builder()
                .firstName("Oscar")
                .lastName("Fernandez")
                .email("oscar@example.com")
                .password("raw-password")
                .build();
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        var response = authenticationService.signup(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFirstName()).isEqualTo("Oscar");
        assertThat(savedUser.getLastName()).isEqualTo("Fernandez");
        assertThat(savedUser.getEmail()).isEqualTo("oscar@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void signinAuthenticatesUserAndReturnsToken() {
        SigninRequest request = SigninRequest.builder()
                .email("oscar@example.com")
                .password("raw-password")
                .build();
        User user = User.builder()
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        when(userRepository.findByEmail("oscar@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        var response = authenticationService.signin(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authenticationCaptor.capture());
        assertThat(authenticationCaptor.getValue().getPrincipal()).isEqualTo("oscar@example.com");
        assertThat(authenticationCaptor.getValue().getCredentials()).isEqualTo("raw-password");
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void signinThrowsWhenAuthenticatedUserCannotBeLoaded() {
        SigninRequest request = SigninRequest.builder()
                .email("missing@example.com")
                .password("raw-password")
                .build();
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.signin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password.");
    }
}
