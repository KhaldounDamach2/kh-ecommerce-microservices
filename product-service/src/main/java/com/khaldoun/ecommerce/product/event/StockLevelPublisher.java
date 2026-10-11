package com.khaldoun.ecommerce.product.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.khaldoun.ecommerce.product.config.KafkaTopicConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockLevelPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Runs only after the stock change is committed, so we never announce
    // a change that was rolled back.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockLevel(StockLevelEvent event) {
        // Only crossings are published, so stock 0 means depleted, otherwise low.
        String topic = event.stock() == 0
                ? KafkaTopicConfig.STOCK_DEPLETED_TOPIC
                : KafkaTopicConfig.STOCK_LOW_TOPIC;

        kafkaTemplate.send(topic, String.valueOf(event.productId()), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish {} for product {}", topic, event.productId(), error);
                    } else {
                        log.info("Published {} for product {} (stock {})",
                                topic, event.productId(), event.stock());
                    }
                });
    }
}
