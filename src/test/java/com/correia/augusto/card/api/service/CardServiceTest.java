package com.correia.augusto.card.api.service;

import com.correia.augusto.card.api.dto.*;
import com.correia.augusto.card.api.entities.*;
import com.correia.augusto.card.api.enums.CardType;
import com.correia.augusto.card.api.exception.*;
import com.correia.augusto.card.api.repository.*;
import com.correia.augusto.card.api.util.EncryptionUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private CardService cardService;

    @Test
    void registerCard_ShouldSaveCardWhenValid() {
        CardRequest request = new CardRequest(
                "1234567890123456",
                "Test User",
                LocalDate.now().plusYears(2),
                "123",
                CardType.CREDIT
        );

        User user = getUser();

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt("1234567890123456")).thenReturn("encryptedNumber");
        when(cardRepository.existsByNumber("encryptedNumber")).thenReturn(false);

        cardService.registerCard(request, "testUser");

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void registerCard_ShouldThrowWhenUserNotFound() {
        CardRequest request = new CardRequest(
                "1234567890123456",
                "Test User",
                LocalDate.now().plusYears(2),
                "123",
                CardType.CREDIT
        );

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cardService.registerCard(request, "testUser"));
    }

    @Test
    void registerCard_ShouldThrowWhenDuplicateCard() {
        CardRequest request = new CardRequest(
                "1234567890123456",
                "Test User",
                LocalDate.now().plusYears(2),
                "123",
                CardType.CREDIT
        );

        User user = getUser();

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt("1234567890123456")).thenReturn("encryptedNumber");
        when(cardRepository.existsByNumber("encryptedNumber")).thenReturn(true);

        assertThrows(DuplicateDataException.class,
                () -> cardService.registerCard(request, "testUser"));
    }

    @Test
    void registerCard_ShouldThrowWhenInvalidExpirationDate() {
        CardRequest request = new CardRequest(
                "1234567890123456",
                "Test User",
                LocalDate.now().minusDays(1),
                "123",
                CardType.CREDIT
        );

        User user = getUser();

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        assertThrows(InvalidCardDataException.class,
                () -> cardService.registerCard(request, "testUser"));
    }

    @Test
    void registerCard_ShouldThrowWhenInvalidCardNumber() {
        CardRequest request = new CardRequest(
                "123", // Número inválido
                "Test User",
                LocalDate.now().plusYears(2),
                "123",
                CardType.CREDIT
        );

        User user = getUser();

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        assertThrows(InvalidCardDataException.class,
                () -> cardService.registerCard(request, "testUser"));
    }

    @Test
    void getCardsByUserId_ShouldReturnCards() {
        Card card1 = new Card();
        card1.setId("1");
        card1.setNumber("encrypted1");

        Card card2 = new Card();
        card2.setId("2");
        card2.setNumber("encrypted2");

        when(cardRepository.findAllByUser(1L)).thenReturn(List.of(card1, card2));
        when(encryptionUtil.decrypt("encrypted1")).thenReturn("123456******7890");
        when(encryptionUtil.decrypt("encrypted2")).thenReturn("987654******3210");

        List<CardResponse> result = cardService.getCardsByUserId(1L);

        assertEquals(2, result.size());
        assertEquals("1234********7890", result.get(0).maskedNumber());
        assertEquals("9876********3210", result.get(1).maskedNumber());
    }

    @Test
    void getCardsByUserId_ShouldReturnEmptyListWhenNoCards() {
        when(cardRepository.findAllByUser(1L)).thenReturn(List.of());

        List<CardResponse> result = cardService.getCardsByUserId(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCardById_ShouldReturnCard() {
        Card card = getCard();

        when(cardRepository.findById("1")).thenReturn(Optional.of(card));
        when(encryptionUtil.decrypt("encrypted")).thenReturn("123456******7890");

        CardResponse result = cardService.getCardById("1");

        assertEquals("1", result.id());
        assertEquals("1234********7890", result.maskedNumber());
        assertEquals("Test User", result.holderName());
    }



    @Test
    void getCardById_ShouldThrowWhenNotFound() {
        when(cardRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cardService.getCardById("1"));
    }

    @Test
    void processCardFile_ShouldProcessValidFile() throws IOException {
        User user = getUser();

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted");
        when(cardRepository.existsByNumber(anyString())).thenReturn(false);

        byte[] fileContent = Files.readAllBytes(
                new ClassPathResource("test-card-file.txt").getFile().toPath());

        ProcessamentoResult result = cardService.processCardFile(fileContent, "testUser");

        assertEquals(10, result.cartoesProcessados());
        assertEquals(12, result.linhasLidas());
        assertEquals("LOTE0001", result.lote());
        verify(cardRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    void processCardFile_ShouldSkipDuplicateCards() throws IOException {
        User user = getUser();

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted");
        when(cardRepository.existsByNumber(anyString())).thenReturn(true); // Todos existem

        byte[] fileContent = Files.readAllBytes(
                new ClassPathResource("test-card-file.txt").getFile().toPath());

        ProcessamentoResult result = cardService.processCardFile(fileContent, "testUser");

        assertEquals(10, result.cartoesProcessados());
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void processCardFile_ShouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        byte[] fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000010".getBytes();

        assertThrows(ResourceNotFoundException.class,
                () -> cardService.processCardFile(fileContent, "testUser"));
    }

    @Test
    void processCardFile_ShouldThrowWhenInvalidHeader() {
        byte[] invalidContent = "INVALID-HEADER".getBytes();

        assertThrows(ResourceNotFoundException.class,
                () -> cardService.processCardFile(invalidContent, "testUser"));
    }

    @Test
    void findByCardNumber_ShouldReturnCard_WhenValid() {
        String cardNumber = "4111111111111111";
        String encrypted = "encrypted-123";
        User user = getUser();
        Card card = getCard();

        when(encryptionUtil.encrypt(cardNumber)).thenReturn(encrypted);
        when(encryptionUtil.decrypt(anyString())).thenReturn(cardNumber);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(cardRepository.findByEncryptedNumber(encrypted)).thenReturn(Optional.of(card));

        CardResponse response = cardService.findByCardNumber(cardNumber, "user1");

        assertNotNull(response);
        verify(encryptionUtil).encrypt(cardNumber);
    }

    @Test
    void findByCardNumber_ShouldThrow_WhenWrongUser() {
        User requester = new User();
        Card card = getCard();

        when(encryptionUtil.encrypt(any())).thenReturn("encrypted");
        when(userRepository.findByUsername("requester")).thenReturn(Optional.of(requester));
        when(cardRepository.findByEncryptedNumber(any())).thenReturn(Optional.of(card));

        assertThrows(AuthenticationFailedException.class,
                () -> cardService.findByCardNumber("4111111111111111", "requester"));
    }

    private static @NotNull Card getCard() {
        Card card = new Card();
        card.setId("1");
        card.setNumber("encripted");
        card.setHolderName("Test User");
        card.setUser(getUser());
        return card;
    }


    private static @NotNull User getUser() {
        User user = new User();
        user.setUsername("testUser");
        user.setId(1L);
        return user;
    }

}