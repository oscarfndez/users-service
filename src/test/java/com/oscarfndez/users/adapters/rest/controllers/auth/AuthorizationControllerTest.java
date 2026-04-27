package com.oscarfndez.users.adapters.rest.controllers.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationControllerTest {

    private final AuthorizationController authorizationController = new AuthorizationController();

    @Test
    void sayHelloReturnsProtectedResourceMessage() {
        var response = authorizationController.sayHello();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Here is your resource");
    }
}
