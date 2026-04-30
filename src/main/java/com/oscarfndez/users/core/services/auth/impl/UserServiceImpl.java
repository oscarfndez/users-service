package com.oscarfndez.users.core.services.auth.impl;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.ports.repositories.UserRepository;
import com.oscarfndez.users.core.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                return userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        };
    }

    @Override
    public Page<User> retrievePage(String search, String sortField, String sortDir, int page, int size) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, mapSortField(sortField)));
        return userRepository.search(normalizeSearch(search), pageable);
    }

    @Override
    public User retrieveOne(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    @Override
    public User update(UUID id, String firstName, String lastName, String email, String role) {
        User user = retrieveOne(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(Role.valueOf(role));
        return userRepository.save(user);
    }

    @Override
    public void deleteOne(UUID id) {
        userRepository.deleteById(id);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }

    private String mapSortField(String sortField) {
        return switch (sortField) {
            case "lastName" -> "lastName";
            case "email" -> "email";
            case "role" -> "role";
            default -> "firstName";
        };
    }
}
