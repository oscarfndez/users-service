package com.oscarfndez.users.core.services.auth.impl;

import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private static final String SIGNING_KEY = "413F4428472B4B6250655368566D5970337336763979244226452948404D6351";

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey", SIGNING_KEY);
    }

    @Test
    void generateTokenIncludesUsernameAndUserRole() {
        User user = User.builder()
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        Claims claims = Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SIGNING_KEY)))
                .build()
                .parseClaimsJws(token)
                .getBody();
        assertThat(jwtService.extractUserName(token)).isEqualTo("oscar@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void isTokenValidReturnsTrueForTokenOwner() {
        User user = User.builder()
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValidReturnsFalseForDifferentUser() {
        User tokenOwner = User.builder()
                .email("oscar@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        User otherUser = User.builder()
                .email("other@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        String token = jwtService.generateToken(tokenOwner);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }
}
