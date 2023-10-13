package com.example.vestrapay.exceptions;

import jakarta.servlet.ServletException;

public class JwtExpiredTokenException extends ServletException {
    public JwtExpiredTokenException(String message) {
        super(message);
    }
}
