package com.oscarfndez.users.core.events;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserEntityListenerTest {

    private final UserLifecycleEventPublisher publisher = mock(UserLifecycleEventPublisher.class);
    private final UserEntityListener listener = listener();

    @Test
    void afterCreatingDelegatesToPublisher() {
        User user = user();

        listener.afterCreating(user);

        verify(publisher).afterCreating(user);
    }

    @Test
    void afterUpdatingDelegatesToPublisher() {
        User user = user();

        listener.afterUpdating(user);

        verify(publisher).afterUpdating(user);
    }

    @Test
    void afterDeletingDelegatesToPublisher() {
        User user = user();

        listener.afterDeleting(user);

        verify(publisher).afterDeleting(user);
    }

    private static User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Oscar")
                .lastName("Fernandez")
                .email("oscar@example.com")
                .role(Role.USER)
                .build();
    }

    private UserEntityListener listener() {
        UserEntityListener listener = new UserEntityListener();
        listener.setPublisher(publisher);
        return listener;
    }
}
