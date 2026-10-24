package com.khaldoun.ecommerce.notification.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.khaldoun.ecommerce.notification.config.KafkaTopicConfig;
import com.khaldoun.ecommerce.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopicConfig.ORDER_STATUS_CHANGED_TOPIC, properties = "spring.json.value.default.type=com.khaldoun.ecommerce.notification.event.OrderStatusChangedEvent")
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Received order-status-changed: order {} is now {}", event.orderId(), event.newStatus());
        notificationService.notifyAboutOrderStatus(event);
    }

}
