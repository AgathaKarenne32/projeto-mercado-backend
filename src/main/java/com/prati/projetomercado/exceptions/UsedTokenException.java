package com.prati.projetomercado.exceptions;

public class UsedTokenException extends RuntimeException {
    public UsedTokenException(String message) {
        super(message);
    }
}
