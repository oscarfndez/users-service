package com.oscarfndez.users.core.services.auth;

import com.oscarfndez.framework.core.model.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface UserService {
    UserDetailsService userDetailsService();

    Page<User> retrievePage(String search, String sortField, String sortDir, int page, int size);

    User retrieveOne(UUID id);

    User create(String firstName, String lastName, String email, String password, String role, MultipartFile photo)
            throws IOException;

    User update(UUID id, String firstName, String lastName, String email, String role);

    User update(UUID id, String firstName, String lastName, String email, String role, MultipartFile photo)
            throws IOException;

    void deleteOne(UUID id);
}
