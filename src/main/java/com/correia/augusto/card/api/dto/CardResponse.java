package com.correia.augusto.card.api.dto;

import com.correia.augusto.card.api.enums.CardType;

import java.time.Instant;
import java.time.LocalDate;

public record CardResponse(
        String id,
        String maskedNumber,
        String holderName,
        LocalDate expirationDate,
        CardType type,
        Instant createdAt
) {}