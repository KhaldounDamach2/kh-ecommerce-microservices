package com.khaldoun.ecommerce.product.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.khaldoun.ecommerce.product.config.KafkaTopicConfig;
import com.khaldoun.ecommerce.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

    private final ProductService productService;

    @KafkaListener(topics = KafkaTopicConfig.ORDER_CREATED_TOPIC)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent {} for order {}", event.eventId(), event.orderId());
        for (OrderCreatedEvent.OrderLine line : event.items()) {
            productService.adjustStock(line.productId(), line.quantity(), "DECREMENT");
        }
    }
}