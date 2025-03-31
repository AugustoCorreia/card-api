package com.correia.augusto.card.api.controller;

import com.correia.augusto.card.api.dto.AuthenticationRequest;
import com.correia.augusto.card.api.dto.AuthenticationResponse;
import com.correia.augusto.card.api.dto.RegisterRequest;
import com.correia.augusto.card.api.security.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();

        registerRequest = new RegisterRequest("userTest", "password123");
        authenticationRequest = new AuthenticationRequest("userTest", "password123");
        authenticationResponse = new AuthenticationResponse("mocked-token");
    }

    @Test
    void testRegister_Success() throws Exception {
        Mockito.when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(authenticationResponse)));
    }

    @Test
    void testAuthenticate_Success() throws Exception {
        Mockito.when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(authenticationResponse);

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(authenticationResponse)));
    }
}
