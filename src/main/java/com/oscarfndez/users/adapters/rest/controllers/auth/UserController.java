package com.oscarfndez.users.adapters.rest.controllers.auth;


import com.oscarfndez.users.adapters.rest.dtos.WhoAmIDto;
import com.oscarfndez.users.ports.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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

        return ResponseEntity.ok(new WhoAmIDto(user.getId(), email, role));
    }
}
