package com.prati.projetomercado.exceptions;

import org.springframework.dao.DataIntegrityViolationException;

public class SupermarketDeletionException extends DataIntegrityViolationException {
    public SupermarketDeletionException(String message) {
        super(message);
    }
}
