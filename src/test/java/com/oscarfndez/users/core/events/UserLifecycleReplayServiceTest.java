package com.oscarfndez.users.core.events;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserLifecycleReplayServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserLifecycleEventPublisher userLifecycleEventPublisher = mock(UserLifecycleEventPublisher.class);
    private final UserLifecycleReplayService service = new UserLifecycleReplayService(userRepository, userLifecycleEventPublisher);

    @Test
    void replayExistingUsersPublishesAfterUpdatingEventForEachUser() {
        User first = user("first@domain.com");
        User second = user("second@domain.com");
        when(userRepository.findAll()).thenReturn(List.of(first, second));

        int replayedUsers = service.replayExistingUsers();

        assertThat(replayedUsers).isEqualTo(2);
        verify(userLifecycleEventPublisher).afterUpdating(first);
        verify(userLifecycleEventPublisher).afterUpdating(second);
    }

    private static User user(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email(email)
                .role(Role.USER)
                .build();
    }
}
