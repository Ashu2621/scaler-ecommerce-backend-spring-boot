package com.scaler.backendproject.payment;

import com.scaler.backendproject.configs.StripeProperties;
import com.scaler.backendproject.models.PaymentStatus;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@ConditionalOnProperty(name = "app.payment.stripe.enabled", havingValue = "true")
@EnableConfigurationProperties(StripeProperties.class)
public class StripePaymentGateway implements PaymentGateway {
    private final StripeProperties properties;
    private final RequestOptions requestOptions;

    public StripePaymentGateway(StripeProperties properties) {
        this.properties = properties;
        this.requestOptions = RequestOptions.builder().setApiKey(properties.apiKey()).build();
    }

    @Override
    public String provider() {
        return "stripe";
    }

    @Override
    public CheckoutSession createCheckout(CheckoutCommand command) {
        long amountInMinorUnit;
        try {
            amountInMinorUnit = command.amount().movePointRight(2).longValueExact();
        } catch (ArithmeticException exception) {
            throw new PaymentGatewayException("INVALID_PAYMENT_AMOUNT", "Payment amount is invalid", exception);
        }
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(command.orderId())
                .setSuccessUrl(properties.successUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(properties.cancelUrl())
                .putMetadata("orderId", command.orderId())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(command.currency().toLowerCase(Locale.ROOT))
                                .setUnitAmount(amountInMinorUnit)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(command.description())
                                        .build())
                                .build())
                        .build())
                .build();
        try {
            Session session = Session.create(params, requestOptions);
            return new CheckoutSession(session.getId(), session.getUrl());
        } catch (StripeException exception) {
            throw new PaymentGatewayException("STRIPE_CHECKOUT_FAILED",
                    "Stripe could not create the checkout session", exception);
        }
    }

    @Override
    public WebhookEvent verifyAndParse(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, properties.webhookSecret());
            StripeObject object = event.getDataObjectDeserializer().getObject().orElse(null);
            if (!(object instanceof Session session)) {
                return new WebhookEvent(event.getId(), "", PaymentStatus.PENDING);
            }
            return new WebhookEvent(
                    event.getId(),
                    session.getId(),
                    mapEvent(event.getType())
            );
        } catch (Exception exception) {
            throw new InvalidWebhookException("INVALID_STRIPE_WEBHOOK",
                    "Stripe webhook signature or payload is invalid", exception);
        }
    }

    @Override
    public PaymentStatus fetchStatus(String providerReference) {
        try {
            Session session = Session.retrieve(providerReference, requestOptions);
            if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
                return PaymentStatus.SUCCEEDED;
            }
            return switch (session.getStatus() == null ? "" : session.getStatus()) {
                case "expired" -> PaymentStatus.CANCELLED;
                default -> PaymentStatus.PENDING;
            };
        } catch (StripeException exception) {
            throw new PaymentGatewayException("STRIPE_RECONCILIATION_FAILED",
                    "Stripe payment status could not be retrieved", exception);
        }
    }

    private PaymentStatus mapEvent(String eventType) {
        return switch (eventType) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                    PaymentStatus.SUCCEEDED;
            case "checkout.session.async_payment_failed" -> PaymentStatus.FAILED;
            case "checkout.session.expired" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }
}
