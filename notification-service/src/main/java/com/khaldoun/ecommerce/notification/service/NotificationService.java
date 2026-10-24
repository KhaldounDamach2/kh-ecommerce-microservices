package com.khaldoun.ecommerce.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khaldoun.ecommerce.notification.dto.NotificationResponse;
import com.khaldoun.ecommerce.notification.exception.NotificationNotFoundException;
import com.khaldoun.ecommerce.notification.domain.Notification;
import com.khaldoun.ecommerce.notification.domain.NotificationType;
import com.khaldoun.ecommerce.notification.domain.ProcessedEvent;
import com.khaldoun.ecommerce.notification.event.OrderStatusChangedEvent;
import com.khaldoun.ecommerce.notification.event.StockLevelEvent;
import com.khaldoun.ecommerce.notification.repository.NotificationRepository;
import com.khaldoun.ecommerce.notification.repository.ProcessedEventRepository;
import com.khaldoun.ecommerce.notification.repository.UserContactRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final UserContactRepository userContactRepository;
    private final EmailService emailService;

    @Transactional
    public void notifySellerAboutStock(StockLevelEvent event, NotificationType type) {
        // Never trust a letter blindly: without these we cannot do anything useful.
        if (event.eventId() == null || event.sellerId() == null || event.productId() == null) {
            log.warn("Ignoring invalid stock event: {}", event);
            return;
        }
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Event {} already processed, skipping", event.eventId());
            return;
        }

        String name = event.productName();
        String title;
        String message;
        if (type == NotificationType.STOCK_DEPLETED) {
            title = "Sold out: " + name;
            message = name + " is out of stock. Restock it to keep selling.";
        } else {
            title = "Low stock: " + name;
            message = "Only " + event.stock() + " left of " + name + ".";
        }

        notificationRepository.save(Notification.builder()
                .userId(event.sellerId())
                .type(type)
                .title(title)
                .message(message)
                .productId(event.productId())
                .build());
        processedEventRepository.save(new ProcessedEvent(event.eventId()));
        log.info("Saved {} notification for seller {}", type, event.sellerId());

        // 1. Look up the seller in our own address book:
        // userContactRepository.findById(event.sellerId())
        // 2. If found: emailService.send(contact.getEmail(), title, message)
        // 3. If not found (sellers who registered before Step 9d):
        // log "No email known for seller {}, in-app only" and do nothing else.
        // Tip: Optional has ifPresentOrElse(found -> ..., () -> ...)
        userContactRepository.findById(event.sellerId())
                .ifPresentOrElse(
                        contact -> emailService.send(contact.getEmail(), title, message),
                        () -> log.info("No email known for seller {}, in-app only", event.sellerId()));
    }

    // The customer hears about CONFIRMED, SHIPPED and CANCELLED; each seller of
    // the order hears about DELIVERED.
    @Transactional
    public void notifyAboutOrderStatus(OrderStatusChangedEvent event) {
        if (event.eventId() == null || event.orderId() == null || event.customerId() == null
                || event.sellerIds() == null || event.newStatus() == null) {
            log.warn("Ignoring invalid order status event: {}", event);
            return;
        }
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Event {} already processed, skipping", event.eventId());
            return;
        }

        Long orderId = event.orderId();
        switch (event.newStatus()) {
            case "CONFIRMED" -> notifyUser(event.customerId(), NotificationType.ORDER_CONFIRMED,
                    "Order #" + orderId + " confirmed",
                    "The seller confirmed your order #" + orderId + " ($" + event.totalPrice() + ").",
                    orderId);
            case "SHIPPED" -> notifyUser(event.customerId(), NotificationType.ORDER_SHIPPED,
                    "Order #" + orderId + " shipped",
                    "Your order #" + orderId + " is on its way.",
                    orderId);
            case "CANCELLED" -> notifyUser(event.customerId(), NotificationType.ORDER_CANCELLED,
                    "Order #" + orderId + " cancelled",
                    "Your order #" + orderId + " was cancelled: " + event.reason(),
                    orderId);
            case "DELIVERED" -> event.sellerIds().forEach(sellerId -> notifyUser(sellerId,
                    NotificationType.ORDER_DELIVERED,
                    "Order #" + orderId + " delivered",
                    "The customer received order #" + orderId + ".",
                    orderId));
            default -> log.info("No notification for status {} of order {}", event.newStatus(), orderId);
        }
        processedEventRepository.save(new ProcessedEvent(event.eventId()));

    }

    // One bell + one email (if we know the address) for one person.
    private void notifyUser(Long userId, NotificationType type, String title, String message, Long orderId) {
        notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .orderId(orderId)
                .build());
        log.info("Saved {} notification for user {}", type, userId);

        userContactRepository.findById(userId)
                .ifPresentOrElse(
                        contact -> emailService.send(contact.getEmail(), title, message),
                        () -> log.info("No email known for user {}, in-app only", userId));

    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long id, Long userId) {
        // Someone else's notification looks exactly like a missing one,
        // so nobody can find out which ids exist.
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsRead(userId);
        log.info("Marked {} notifications as read for user {}", updated, userId);
    }

}
