package com.correia.augusto.card.api.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationProviderTest {

    @InjectMocks
    private CustomAuthenticationProvider authenticationProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final String username = "testUser";
    private final String password = "password123";
    private final String encodedPassword = "encodedPassword";





    @Test
    void testAuthenticate_Success() {
        UserDetails userDetails = new User(username, encodedPassword, Collections.emptyList());

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        Authentication result = authenticationProvider.authenticate(authentication);

        assertNotNull(result);
        assertEquals(username, result.getName());
        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void testAuthenticate_InvalidCredentials() {
        UserDetails userDetails = new User(username, encodedPassword, Collections.emptyList());

        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        assertThrows(BadCredentialsException.class, () -> authenticationProvider.authenticate(authentication));
    }

    @Test
    void testSupports_ValidAuthenticationClass() {
        assertTrue(authenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
    }
}
