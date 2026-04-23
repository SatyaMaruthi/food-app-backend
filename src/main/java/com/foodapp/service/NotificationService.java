package com.foodapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@ConditionalOnBean(JavaMailSender.class)
public class NotificationService {
    private final JavaMailSender mailSender;
    private final Queue<MailTask> retryQueue = new ConcurrentLinkedQueue<>();

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyOrderCreated(String userEmail, String sellerEmail, String deliveryPartnerEmail, Long orderId) {
        sendOrQueue(userEmail, "Order confirmed #" + orderId);
        sendOrQueue(sellerEmail, "New order received #" + orderId);
        sendOrQueue(deliveryPartnerEmail, "Delivery assigned for order #" + orderId);
    }

    @Scheduled(fixedDelay = 120000)
    public void retryFailedMails() {
        int retries = retryQueue.size();
        for (int i = 0; i < retries; i++) {
            MailTask task = retryQueue.poll();
            if (task != null) {
                sendOrQueue(task.to(), task.body());
            }
        }
    }

    private void sendOrQueue(String to, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Food App Order Update");
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Mail send failed for {}, scheduling retry", to);
            retryQueue.offer(new MailTask(to, body));
        }
    }

    private record MailTask(String to, String body) {}
}
