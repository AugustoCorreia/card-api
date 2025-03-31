package com.correia.augusto.card.api.controller;

import com.correia.augusto.card.api.dto.CardRequest;
import com.correia.augusto.card.api.dto.CardResponse;
import com.correia.augusto.card.api.dto.ProcessamentoResult;
import com.correia.augusto.card.api.enums.CardType;
import com.correia.augusto.card.api.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
 class CardControllerTest {

    @InjectMocks
    private CardController cardController;

    @Mock
    private CardService cardService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private MultipartFile file;

    private CardRequest cardRequest;
    private CardResponse cardResponse;

    @BeforeEach
     void setup() {
        cardRequest = new CardRequest("1234567890123456", "John Doe",  LocalDate.now().plusYears(1),"123", CardType.PREPAID);
        cardResponse = new CardResponse("cardId", "1234567890123456", "John Doe",  LocalDate.now().plusYears(1), CardType.DEBIT, Instant.now());
    }

    @Test
     void testRegisterCard() {
        when(userDetails.getUsername()).thenReturn("user123");

        ResponseEntity<Void> response = cardController.registerCard(cardRequest, userDetails);

        verify(cardService).registerCard(cardRequest, "user123");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
     void testGetCardsByUser() {
        when(cardService.getCardsByUserId(1L)).thenReturn(Collections.singletonList(cardResponse));

        ResponseEntity<List<CardResponse>> response = cardController.getCardsByUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals("cardId", response.getBody().get(0).id());
    }

    @Test
     void testGetCardById() {
        when(cardService.getCardById("cardId")).thenReturn(cardResponse);

        ResponseEntity<CardResponse> response = cardController.getCardById("cardId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("cardId", response.getBody().id());
    }

    @Test
     void testUploadArquivoCartoes() throws IOException {
        byte[] fileBytes = "test file".getBytes();
        when(file.getBytes()).thenReturn(fileBytes);
        when(userDetails.getUsername()).thenReturn("user123");

        ProcessamentoResult processamentoResult = new ProcessamentoResult(5, 10, "batch1"); // Exemplo de resultado
        when(cardService.processCardFile(fileBytes, "user123")).thenReturn(processamentoResult);

        ResponseEntity<ProcessamentoResult> response = cardController.uploadArquivoCartoes(file, userDetails);

        verify(cardService).processCardFile(fileBytes, "user123");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().cartoesProcessados());
        assertEquals(10, response.getBody().linhasLidas());
        assertEquals("batch1", response.getBody().lote());
    }

    @Test
     void testUploadArquivoCartoes_FileEmpty() throws IOException {
        when(file.isEmpty()).thenReturn(true);

        ResponseEntity<ProcessamentoResult> response = cardController.uploadArquivoCartoes(file, userDetails);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
     void testRegisterCard_UserServiceThrowsException() {
        when(userDetails.getUsername()).thenReturn("user123");
        doThrow(new RuntimeException("Service exception")).when(cardService).registerCard(any(), anyString());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardController.registerCard(cardRequest, userDetails));

        assertEquals("Service exception", exception.getMessage());
    }

   @Test
   void test_returns_card_response_for_valid_card_number() {
      String validCardNumber = "4111111111111111";
      String username = "testuser";

      CardResponse expectedResponse = new CardResponse(
              "card-id-123",
              "4111************11",
              "Test User",
              LocalDate.of(2025, 12, 31),
              CardType.CREDIT,
              Instant.now()
      );

      when(userDetails.getUsername()).thenReturn(username);
      when(cardService.findByCardNumber(validCardNumber, username)).thenReturn(expectedResponse);

      ResponseEntity<CardResponse> response = cardController.getCardByNumber(validCardNumber, userDetails);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(expectedResponse, response.getBody());
      verify(cardService).findByCardNumber(validCardNumber, username);
   }
}
