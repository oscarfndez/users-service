package com.oscarfndez.users.core.services.auth.impl;


import com.oscarfndez.users.adapters.rest.dtos.auth.SignUpRequest;
import com.oscarfndez.users.adapters.rest.dtos.auth.SigninRequest;
import com.oscarfndez.users.adapters.rest.dtos.auth.JwtAuthenticationResponse;
import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.ports.repositories.UserRepository;
import com.oscarfndez.users.core.services.auth.AuthenticationService;
import com.oscarfndez.users.core.services.auth.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public JwtAuthenticationResponse signup(SignUpRequest request) {
        var user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
                .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER).build();
        user = userRepository.save(user);
        var jwt = jwtService.generateToken(user);
        log.info("User signed up email={} role={}", user.getEmail(), user.getRole());
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }

    @Override
    public JwtAuthenticationResponse signin(SigninRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        var jwt = jwtService.generateToken(user);
        log.info("User signed in email={} role={}", user.getEmail(), user.getRole());
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }
}
