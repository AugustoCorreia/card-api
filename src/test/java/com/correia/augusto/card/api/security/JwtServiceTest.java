package com.correia.augusto.card.api.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        SecretKey key = Keys.hmacShaKeyFor(new SecureRandom().generateSeed(32));
        String secretKey = Base64.getEncoder().encodeToString(key.getEncoded());

        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);

        long jwtExpiration = 1000 * 60 * 60;
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);

        long refreshExpiration = 1000 * 60 * 60 * 24;
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        UserDetails user = new User("testUser", "password", Collections.emptyList());

        String token = jwtService.generateToken(user);

        assertNotNull(token);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals("testUser", extractedUsername);
    }

    @Test
    void testIsTokenValid() {
        UserDetails user = new User("validUser", "password", Collections.emptyList());

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void testIsTokenExpired() throws InterruptedException {
        UserDetails user = new User("expiredUser", "password", Collections.emptyList());
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);

        String token = jwtService.generateToken(user);

        Thread.sleep(2);

        assertThrows(ExpiredJwtException.class,()->jwtService.isTokenValid(token, user));
    }
}
