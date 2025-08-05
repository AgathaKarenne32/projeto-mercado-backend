package com.prati.projetomercado.utils;

import jakarta.servlet.http.HttpServletRequest;

public class TokenUtils {
    public static String recoveryToken(String headerAuthorization) {
        if (headerAuthorization != null && headerAuthorization.startsWith("Bearer ")) {
            return headerAuthorization.substring(7);
        }

        return null;
    }
}
