package com.oscarfndez.users.core.services.auth.impl;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void userDetailsServiceLoadsUserByEmail() {
        User user = User.builder()
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        when(userRepository.findByEmail("oscar@example.com")).thenReturn(Optional.of(user));

        var loadedUser = userService.userDetailsService().loadUserByUsername("oscar@example.com");

        assertThat(loadedUser).isSameAs(user);
    }

    @Test
    void userDetailsServiceThrowsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.userDetailsService().loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void retrievePageBuildsPageableWithMappedSortAndTrimmedSearch() {
        var page = new PageImpl<>(List.of(user(UUID.randomUUID())));
        when(userRepository.search(any(), any(Pageable.class))).thenReturn(page);

        assertThat(userService.retrievePage(" oscar ", "email", "desc", 2, 25)).isSameAs(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).search(eq("oscar"), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        Sort.Order order = pageable.getSort().getOrderFor("email");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void updateChangesEditableFieldsAndPersistsUser() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User updated = userService.update(userId, "Ana", "Garcia", "ana@example.com", "ADMIN");

        assertThat(updated.getFirstName()).isEqualTo("Ana");
        assertThat(updated.getLastName()).isEqualTo("Garcia");
        assertThat(updated.getEmail()).isEqualTo("ana@example.com");
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    void deleteOneDelegatesToRepository() {
        UUID userId = UUID.randomUUID();

        userService.deleteOne(userId);

        verify(userRepository).deleteById(userId);
    }

    private static User user(UUID userId) {
        return User.builder()
                .id(userId)
                .firstName("Oscar")
                .lastName("Fernandez")
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
    }
}
