package com.oscarfndez.users.adapters.rest.controllers.auth;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    @Test
    void whoAmIReturnsAuthenticatedUserEmailAndFirstRole() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByEmail("oscar@example.com")).thenReturn(Optional.of(user(userId)));
        var authentication = new UsernamePasswordAuthenticationToken(
                "oscar@example.com",
                null,
                List.of(new SimpleGrantedAuthority("USER")));

        var response = userController.whoAmI(authentication);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
        assertThat(response.getBody().getEmail()).isEqualTo("oscar@example.com");
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    void whoAmIReturnsUnknownRoleWhenAuthenticationHasNoAuthorities() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByEmail("oscar@example.com")).thenReturn(Optional.of(user(userId)));
        var authentication = new UsernamePasswordAuthenticationToken("oscar@example.com", null, List.of());

        var response = userController.whoAmI(authentication);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
        assertThat(response.getBody().getEmail()).isEqualTo("oscar@example.com");
        assertThat(response.getBody().getRole()).isEqualTo("UNKNOWN");
    }

    private static User user(UUID userId) {
        return User.builder()
                .id(userId)
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
    }
}
