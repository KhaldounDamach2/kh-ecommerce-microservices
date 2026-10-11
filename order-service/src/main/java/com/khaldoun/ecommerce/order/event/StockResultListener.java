package com.khaldoun.ecommerce.order.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.khaldoun.ecommerce.order.config.KafkaTopicConfig;
import com.khaldoun.ecommerce.order.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockResultListener {

    private final OrderService orderService;

    @KafkaListener(topics = KafkaTopicConfig.STOCK_RESULT_TOPIC)
    public void onStockResult(StockResultEvent event) {
        log.info("Received StockResultEvent for order {}: reserved={}", event.orderId(), event.reserved());
        if (!event.reserved()) {
            orderService.cancelIfRejected(event.orderId(), event.reason());
        }
    }
}