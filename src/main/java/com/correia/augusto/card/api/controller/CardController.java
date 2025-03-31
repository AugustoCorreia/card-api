package com.correia.augusto.card.api.controller;

import com.correia.augusto.card.api.dto.CardRequest;
import com.correia.augusto.card.api.dto.CardResponse;
import com.correia.augusto.card.api.dto.ProcessamentoResult;
import com.correia.augusto.card.api.service.CardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Cards Operations Related")
@SecurityRequirement(name = "JWT")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> registerCard(
            @Valid @RequestBody CardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        cardService.registerCard(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardResponse>> getCardsByUser(@PathVariable Long userId) {
        List<CardResponse> cards = cardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable String cardId) {
        CardResponse card = cardService.getCardById(cardId);
        return ResponseEntity.ok(card);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/upload")
    public ResponseEntity<ProcessamentoResult> uploadArquivoCartoes(
            @RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ProcessamentoResult resultant = cardService.processCardFile(file.getBytes(), userDetails.getUsername());
        return ResponseEntity.ok(resultant);
    }

    @GetMapping("/by-number")
    public ResponseEntity<CardResponse> getCardByNumber(
            @RequestParam String number,
            @AuthenticationPrincipal UserDetails userDetails) {

        CardResponse response = cardService.findByCardNumber(number, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
