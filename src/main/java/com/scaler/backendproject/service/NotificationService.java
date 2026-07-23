package com.scaler.backendproject.service;

import com.scaler.backendproject.events.OrderPaidEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;
    private final String from;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${app.notifications.email.enabled:false}") boolean emailEnabled,
                               @Value("${app.notifications.email.from:no-reply@example.com}") String from) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
        this.from = from;
    }

    public void sendPaymentReceipt(OrderPaidEvent event) {
        if (!emailEnabled) {
            log.info("Email disabled; receipt event recorded for orderId={}", event.orderId());
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(event.customerEmail());
        message.setSubject("Payment received for order " + event.orderId());
        message.setText("We received " + event.currency() + " " + event.amount()
                + " for order " + event.orderId() + ".");
        mailSender.send(message);
    }
}
