package com.correia.augusto.card.api.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " com ID " + id + " não encontrado");
    }
}