package com.khaldoun.ecommerce.product.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.khaldoun.ecommerce.product.config.KafkaTopicConfig;
import com.khaldoun.ecommerce.product.service.ProductService;
import com.khaldoun.ecommerce.product.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaTopicConfig.ORDER_CREATED_TOPIC)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent {} for order {}", event.eventId(), event.orderId());
        productService.reserveStockForOrder(event).ifPresent(result -> {
            kafkaTemplate.send(KafkaTopicConfig.STOCK_RESULT_TOPIC, String.valueOf(result.orderId()), result);
            log.info("Published stock result for order {}: reserved={}", result.orderId(), result.reserved());
        });
    }
}
