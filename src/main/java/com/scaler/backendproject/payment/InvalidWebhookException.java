package com.scaler.backendproject.payment;

import com.scaler.backendproject.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidWebhookException extends ApiException {
    public InvalidWebhookException(String code, String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, code, message);
        initCause(cause);
    }

    public InvalidWebhookException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
