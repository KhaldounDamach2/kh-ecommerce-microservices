package com.khaldoun.ecommerce.notification.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.khaldoun.ecommerce.notification.config.KafkaTopicConfig;
import com.khaldoun.ecommerce.notification.domain.NotificationType;
import com.khaldoun.ecommerce.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockLevelListener {

    private final NotificationService notificationService;

    // One listener, two topics. The topic name (written on the envelope)
    // tells us which bell rang.
    @KafkaListener(topics = { KafkaTopicConfig.STOCK_LOW_TOPIC, KafkaTopicConfig.STOCK_DEPLETED_TOPIC })
    public void onStockLevel(StockLevelEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received {} for product {} (stock {})", topic, event.productId(), event.stock());
        NotificationType type = KafkaTopicConfig.STOCK_DEPLETED_TOPIC.equals(topic)
                ? NotificationType.STOCK_DEPLETED
                : NotificationType.STOCK_LOW;
        notificationService.notifySellerAboutStock(event, type);
    }
}
