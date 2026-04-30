package com.oscarfndez.users.core.services.auth;

import com.oscarfndez.framework.core.model.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService {
    UserDetailsService userDetailsService();

    Page<User> retrievePage(String search, String sortField, String sortDir, int page, int size);

    User retrieveOne(UUID id);

    User update(UUID id, String firstName, String lastName, String email, String role);

    void deleteOne(UUID id);
}
