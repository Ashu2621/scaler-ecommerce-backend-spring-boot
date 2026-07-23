package com.scaler.backendproject.payment;

import com.scaler.backendproject.exceptions.ApiException;
import org.springframework.http.HttpStatus;

public class PaymentGatewayException extends ApiException {
    public PaymentGatewayException(String code, String message, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, code, message);
        initCause(cause);
    }

    public PaymentGatewayException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
