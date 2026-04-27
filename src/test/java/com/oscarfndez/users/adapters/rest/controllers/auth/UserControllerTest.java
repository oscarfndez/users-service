package com.oscarfndez.users.adapters.rest.controllers.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest {

    private final UserController userController = new UserController();

    @Test
    void whoAmIReturnsAuthenticatedUserEmailAndFirstRole() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "oscar@example.com",
                null,
                List.of(new SimpleGrantedAuthority("USER")));

        var response = userController.whoAmI(authentication);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("oscar@example.com");
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    void whoAmIReturnsUnknownRoleWhenAuthenticationHasNoAuthorities() {
        var authentication = new UsernamePasswordAuthenticationToken("oscar@example.com", null, List.of());

        var response = userController.whoAmI(authentication);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("oscar@example.com");
        assertThat(response.getBody().getRole()).isEqualTo("UNKNOWN");
    }
}
