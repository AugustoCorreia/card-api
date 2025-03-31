package com.correia.augusto.card.api.exception;

public class InvalidCardDataException extends RuntimeException {
    public InvalidCardDataException(String message) {
        super(message);
    }
}