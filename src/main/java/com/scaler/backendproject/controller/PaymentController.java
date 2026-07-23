package com.scaler.backendproject.controller;

import com.scaler.backendproject.dto.CreatePaymentRequest;
import com.scaler.backendproject.dto.PaymentResponse;
import com.scaler.backendproject.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(Authentication authentication,
                                  @PathVariable String orderId,
                                  @Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createCheckout(authentication.getName(), orderId, request.provider());
    }

    @GetMapping("/orders/{orderId}")
    public PaymentResponse find(Authentication authentication, @PathVariable String orderId) {
        return paymentService.findForOrder(authentication.getName(), orderId);
    }

    @PostMapping("/webhooks/{provider}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void webhook(@PathVariable String provider,
                        @RequestBody String payload,
                        @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature,
                        @RequestHeader(value = "X-Mock-Signature", required = false) String mockSignature) {
        String signature = "stripe".equalsIgnoreCase(provider) ? stripeSignature : mockSignature;
        paymentService.handleWebhook(provider, payload, signature);
    }
}
