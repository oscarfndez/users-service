package com.oscarfndez.users.core.services.auth;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.ports.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasRoleReturnsTrueWhenAuthenticatedUserHasRoleAndExists() {
        User user = User.builder()
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(userRepository.findByEmail("oscar@example.com")).thenReturn(Optional.of(user));

        assertThat(authorizationService.hasRole("USER")).isTrue();
    }

    @Test
    void hasRoleReturnsFalseWhenAuthenticatedUserDoesNotHaveRole() {
        User user = User.builder()
                .email("admin@example.com")
                .password("encoded-password")
                .role(Role.ADMIN)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        assertThat(authorizationService.hasRole("USER")).isFalse();
    }

    @Test
    void hasRoleReturnsFalseWhenAuthenticatedUserNoLongerExists() {
        User user = User.builder()
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(userRepository.findByEmail("oscar@example.com")).thenReturn(Optional.empty());

        assertThat(authorizationService.hasRole("USER")).isFalse();
    }

    @Test
    void hasRoleReturnsFalseWhenThereIsNoAuthenticatedUser() {
        assertThat(authorizationService.hasRole("USER")).isFalse();
    }
}
