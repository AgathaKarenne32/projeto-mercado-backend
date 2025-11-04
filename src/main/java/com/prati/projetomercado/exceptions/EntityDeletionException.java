package com.prati.projetomercado.exceptions;

import org.springframework.dao.DataIntegrityViolationException;

public class EntityDeletionException extends DataIntegrityViolationException {
    public EntityDeletionException(String message) {
        super(message);
    }
}
