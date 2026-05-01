package com.oscarfndez.users.adapters.rest.controllers.auth;


import com.oscarfndez.users.adapters.rest.dtos.WhoAmIDto;
import com.oscarfndez.users.ports.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/api/whoami")
    public ResponseEntity<WhoAmIDto> whoAmI(Authentication authentication) {
        String email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("UNKNOWN");

        return ResponseEntity.ok(new WhoAmIDto(
                user.getId(),
                email,
                role,
                user.getFirstName(),
                user.getLastName(),
                user.getPhoto() != null && user.getPhoto().length > 0
        ));
    }

    @GetMapping("/api/whoami/photo")
    public ResponseEntity<byte[]> whoAmIPhoto(Authentication authentication) {
        String email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        if (user.getPhoto() == null || user.getPhoto().length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, user.getPhotoContentType() != null
                        ? user.getPhotoContentType()
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(user.getPhoto());
    }
}
