package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.PaymentResponse;
import com.scaler.backendproject.events.OrderPaidEvent;
import com.scaler.backendproject.exceptions.BadRequestException;
import com.scaler.backendproject.exceptions.NotFoundException;
import com.scaler.backendproject.models.CustomerOrder;
import com.scaler.backendproject.models.OrderStatus;
import com.scaler.backendproject.models.Payment;
import com.scaler.backendproject.models.PaymentStatus;
import com.scaler.backendproject.models.ProcessedWebhookEvent;
import com.scaler.backendproject.models.Product;
import com.scaler.backendproject.payment.PaymentGateway;
import com.scaler.backendproject.payment.PaymentGatewayResolver;
import com.scaler.backendproject.repository.OrderRepository;
import com.scaler.backendproject.repository.PaymentRepository;
import com.scaler.backendproject.repository.ProcessedWebhookEventRepository;
import com.scaler.backendproject.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProcessedWebhookEventRepository webhookEventRepository;
    private final ProductRepository productRepository;
    private final PaymentGatewayResolver gatewayResolver;
    private final OutboxService outboxService;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          ProcessedWebhookEventRepository webhookEventRepository,
                          ProductRepository productRepository,
                          PaymentGatewayResolver gatewayResolver,
                          OutboxService outboxService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.productRepository = productRepository;
        this.gatewayResolver = gatewayResolver;
        this.outboxService = outboxService;
    }

    @Transactional
    public PaymentResponse createCheckout(String customerEmail, String orderId, String provider) {
        CustomerOrder order = orderRepository.findOwnedById(orderId, customerEmail)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("ORDER_NOT_PAYABLE",
                    "Only pending orders can start a payment");
        }
        var existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }
        PaymentGateway gateway = gatewayResolver.resolve(provider);
        PaymentGateway.CheckoutSession checkout = gateway.createCheckout(
                new PaymentGateway.CheckoutCommand(
                        order.getId(),
                        order.getTotalAmount(),
                        order.getCurrency(),
                        "E-commerce order " + order.getId()
                )
        );
        Payment payment = paymentRepository.save(new Payment(
                order, gateway.provider(), checkout.providerReference(), checkout.checkoutUrl()));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse findForOrder(String customerEmail, String orderId) {
        orderRepository.findOwnedById(orderId, customerEmail)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        return paymentRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Payment for order", orderId));
    }

    @Transactional
    public void handleWebhook(String provider, String payload, String signature) {
        PaymentGateway gateway = gatewayResolver.resolve(provider);
        PaymentGateway.WebhookEvent webhook = gateway.verifyAndParse(payload, signature);
        if (webhookEventRepository.existsByProviderAndProviderEventId(
                gateway.provider(), webhook.providerEventId())) {
            return;
        }
        webhookEventRepository.save(new ProcessedWebhookEvent(
                gateway.provider(), webhook.providerEventId()));
        if (webhook.providerReference() == null || webhook.providerReference().isBlank()) {
            return;
        }
        Payment payment = paymentRepository.findByProviderAndProviderReference(
                        gateway.provider(), webhook.providerReference())
                .orElseThrow(() -> new NotFoundException("Payment", webhook.providerReference()));
        applyStatus(payment, webhook.status());
    }

    @Scheduled(fixedDelayString = "${app.payment.reconciliation-delay-ms:300000}")
    @Transactional
    public void reconcilePendingPayments() {
        List<Payment> pending = paymentRepository.findTop100ByStatusInAndUpdatedAtBefore(
                List.of(PaymentStatus.PENDING),
                Instant.now().minus(5, ChronoUnit.MINUTES)
        );
        for (Payment payment : pending) {
            PaymentStatus status = gatewayResolver.resolve(payment.getProvider())
                    .fetchStatus(payment.getProviderReference());
            applyStatus(payment, status);
        }
    }

    private void applyStatus(Payment payment, PaymentStatus status) {
        PaymentStatus previous = payment.getStatus();
        payment.apply(status);
        CustomerOrder order = payment.getOrder();
        if (status == PaymentStatus.SUCCEEDED) {
            order.markPaid();
            if (previous != PaymentStatus.SUCCEEDED) {
                outboxService.enqueue(new OrderPaidEvent(
                        UUID.randomUUID().toString(),
                        order.getId(),
                        order.getCustomerEmail(),
                        order.getTotalAmount(),
                        order.getCurrency(),
                        Instant.now()
                ));
            }
        } else if (status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED) {
            order.markPaymentFailed();
            if (previous == PaymentStatus.PENDING) {
                releaseInventory(order);
            }
        }
        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    private void releaseInventory(CustomerOrder order) {
        List<String> productIds = order.getItems().stream()
                .map(item -> item.getProductId())
                .distinct()
                .sorted()
                .toList();
        Map<String, Product> products = productRepository.findAllByIdForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        order.getItems().forEach(item -> {
            Product product = products.get(item.getProductId());
            if (product != null) {
                product.release(item.getQuantity());
            }
        });
        productRepository.saveAll(products.values());
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getProvider(),
                payment.getCheckoutUrl(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
