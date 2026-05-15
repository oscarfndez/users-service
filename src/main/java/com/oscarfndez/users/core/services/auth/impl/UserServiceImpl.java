package com.oscarfndez.users.core.services.auth.impl;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.ports.repositories.UserRepository;
import com.oscarfndez.users.core.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
    @Cacheable(cacheNames = "userPages", key = "{#p0 == null ? '' : #p0.trim(), #p1, #p2, #p3, #p4}")
    public Page<User> retrievePage(String search, String sortField, String sortDir, int page, int size) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, mapSortField(sortField)));
        return userRepository.search(normalizeSearch(search), pageable);
    }

    @Override
    @Cacheable(cacheNames = "users", key = "#p0")
    public User retrieveOne(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    @Override
    @CacheEvict(cacheNames = "userPages", allEntries = true)
    public User create(String firstName, String lastName, String email, String password, String role, MultipartFile photo)
            throws IOException {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.valueOf(role))
                .build();

        if (photo != null && !photo.isEmpty()) {
            user.setPhoto(photo.getBytes());
            user.setPhotoContentType(photo.getContentType());
        }

        return userRepository.save(user);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "users", key = "#p0"),
            @CacheEvict(cacheNames = "userPages", allEntries = true)
    })
    public User update(UUID id, String firstName, String lastName, String email, String role) {
        return userRepository.save(updateExistingUser(id, firstName, lastName, email, role));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "users", key = "#p0"),
            @CacheEvict(cacheNames = "userPages", allEntries = true)
    })
    public User update(UUID id, String firstName, String lastName, String email, String role, MultipartFile photo)
            throws IOException {
        User user = updateExistingUser(id, firstName, lastName, email, role);
        if (photo != null && !photo.isEmpty()) {
            user.setPhoto(photo.getBytes());
            user.setPhotoContentType(photo.getContentType());
        }

        return userRepository.save(user);
    }

    private User updateExistingUser(UUID id, String firstName, String lastName, String email, String role) {
        User user = retrieveOne(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(Role.valueOf(role));
        return user;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "users", key = "#p0"),
            @CacheEvict(cacheNames = "userPages", allEntries = true)
    })
    public void deleteOne(UUID id) {
        User user = retrieveOne(id);
        userRepository.delete(user);
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
