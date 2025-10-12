package com.prati.projetomercado.service;

import com.prati.projetomercado.entity.AuthUser;

public interface EmailService {
    void sendConfirmationEmail(AuthUser user);
}