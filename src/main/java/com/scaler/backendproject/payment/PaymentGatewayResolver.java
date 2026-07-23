package com.scaler.backendproject.payment;

import com.scaler.backendproject.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayResolver {
    private final Map<String, PaymentGateway> gateways;

    public PaymentGatewayResolver(List<PaymentGateway> gateways) {
        this.gateways = gateways.stream().collect(Collectors.toUnmodifiableMap(
                gateway -> gateway.provider().toLowerCase(Locale.ROOT),
                Function.identity()
        ));
    }

    public PaymentGateway resolve(String provider) {
        PaymentGateway gateway = gateways.get(provider.toLowerCase(Locale.ROOT));
        if (gateway == null) {
            throw new BadRequestException("UNSUPPORTED_PAYMENT_PROVIDER",
                    "Unsupported payment provider: " + provider);
        }
        return gateway;
    }
}
