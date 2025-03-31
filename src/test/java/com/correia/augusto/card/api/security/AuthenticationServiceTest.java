package com.correia.augusto.card.api.security;

import com.correia.augusto.card.api.dto.AuthenticationRequest;
import com.correia.augusto.card.api.dto.AuthenticationResponse;
import com.correia.augusto.card.api.dto.RegisterRequest;
import com.correia.augusto.card.api.entities.User;
import com.correia.augusto.card.api.exception.DuplicateDataException;
import com.correia.augusto.card.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private final String username = "testUser";
    private final String encodedPassword = "encodedPassword";
    private final String password = "password123";
    private final String jwtToken = "jwtToken";

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest(username, password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(jwtService.generateToken(any(User.class))).thenReturn(jwtToken);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        AuthenticationResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateUser() {
        RegisterRequest request = new RegisterRequest(username, password);

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateDataException.class, () -> authenticationService.register(request));
    }

    @Test
    void testAuthenticate_Success() {
        AuthenticationRequest request = new AuthenticationRequest(username, password);
        User user = new User();

        when(jwtService.generateToken(user)).thenReturn(jwtToken);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals(user, userRepository.findByUsername(username).orElse(null));
    }

    @Test
    void testAuthenticate_Failure() {
        AuthenticationRequest request = new AuthenticationRequest(username, "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));
    }
}
