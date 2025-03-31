package com.correia.augusto.card.api.service;

import com.correia.augusto.card.api.dto.CardRequest;
import com.correia.augusto.card.api.dto.CardResponse;
import com.correia.augusto.card.api.dto.ProcessamentoResult;
import com.correia.augusto.card.api.entities.Card;
import com.correia.augusto.card.api.entities.User;
import com.correia.augusto.card.api.enums.CardType;
import com.correia.augusto.card.api.exception.DuplicateDataException;
import com.correia.augusto.card.api.exception.InvalidCardDataException;
import com.correia.augusto.card.api.exception.ResourceNotFoundException;
import com.correia.augusto.card.api.repository.CardRepository;
import com.correia.augusto.card.api.repository.UserRepository;
import com.correia.augusto.card.api.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private static final int BATCH_SIZE = 100;

    public void registerCard(CardRequest request, String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userName));

        if (cardRepository.existsByNumber(encryptionUtil.encrypt(request.number()))) {
            throw new DuplicateDataException("Cartão já cadastrado para este usuário");
        }

        validateCard(request);

        Card card = toEntity(request);
        card.setUser(user);

        cardRepository.save(card);
    }

    public List<CardResponse> getCardsByUserId(Long userId) {
        return cardRepository.findAllByUser(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CardResponse getCardById(String cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão", cardId));

        return toResponse(card);
    }

    private void validateCard(CardRequest request) {
        if (request.expirationDate().isBefore(LocalDate.now())) {
            throw new InvalidCardDataException("Data de expiração inválida");
        }

        if (!request.number().matches("^\\d{13,19}$")) {
            throw new InvalidCardDataException("Número do cartão inválido");
        }
    }

    private Card toEntity(CardRequest request) {
        return Card.builder()
                .number(encryptionUtil.encrypt(request.number()))
                .holderName(request.holderName())
                .expirationDate(request.expirationDate())
                .cvv(request.cvv())
                .type(request.type())
                .build();
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                maskCardNumber(encryptionUtil.decrypt(card.getNumber())),
                card.getHolderName(),
                card.getExpirationDate(),
                card.getType(),
                card.getCreatedAt()
        );
    }

    @Transactional
    public ProcessamentoResult processCardFile(byte[] conteudoArquivo, String username) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(conteudoArquivo)))) {

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", username));

            String header = reader.readLine();
            String nome = header.substring(0, 29).trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate dataProcessamento = LocalDate.parse(header.substring(29, 37).trim(), formatter);

            String lote = header.substring(37, 45).trim();
            int qtdRegistros = Integer.parseInt(header.substring(45, 51).trim());

            List<Card> batch = new ArrayList<>(BATCH_SIZE);
            int cartoesProcessados = 0;
            int linhasLidas = 1;

            String linha;
            while (cartoesProcessados < qtdRegistros && (linha = reader.readLine()) != null) {
                linhasLidas++;

                String cardNumber = linha.substring(7, linha.length() - 1).trim();
                if (cardNumber.trim().length() >= 13 && cardNumber.trim().length() <= 19) {

                    String encryptedNumber = encryptionUtil.encrypt(cardNumber.trim());

                    boolean existsInBatch = batch.stream().anyMatch(card -> card.getNumber().equals(encryptedNumber));

                    if (!existsInBatch && !cardRepository.existsByNumber(encryptedNumber)) {
                        Card card = new Card();
                        card.setNumber(encryptedNumber);
                        card.setLote(lote);
                        card.setDataProcessamento(dataProcessamento);
                        card.setType(CardType.CREDIT);
                        card.setHolderName(nome);
                        card.setCvv("000");
                        card.setExpirationDate(LocalDate.of(2099, 12, 31));
                        card.setUser(user);
                        batch.add(card);

                        if (batch.size() >= BATCH_SIZE) {
                            cardRepository.saveAll(batch);
                            batch.clear();
                        }
                        cartoesProcessados++;
                    } else {
                        log.info("Card number already exists: {}", encryptedNumber);
                    }
                }
            }

            if (!batch.isEmpty()) {
                cardRepository.saveAll(batch);
            }

            return new ProcessamentoResult(cartoesProcessados, linhasLidas, lote);
        }
    }

    private String maskCardNumber(String number) {
        return number.replaceAll("(?<=.{4}).(?=.{4})", "*");
    }
}