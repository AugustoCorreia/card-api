package com.correia.augusto.card.api.dto;

import com.correia.augusto.card.api.enums.CardType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CardRequest(
        @NotBlank @Size(min = 13, max = 19) String number,
        @NotBlank @Size(min = 2, max = 50) String holderName,
        @NotNull @Future LocalDate expirationDate,
        @NotBlank @Size(min = 3, max = 4) String cvv,
        @NotNull CardType type
) {}
