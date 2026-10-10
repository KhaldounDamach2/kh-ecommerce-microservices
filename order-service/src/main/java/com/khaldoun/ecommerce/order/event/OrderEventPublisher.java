package com.khaldoun.ecommerce.order.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.khaldoun.ecommerce.order.config.KafkaTopicConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.ORDER_CREATED_TOPIC, String.valueOf(event.orderId()), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish OrderCreatedEvent for order {}", event.orderId(), error);
                    } else {
                        log.info("Published OrderCreatedEvent for order {} to partition {} offset {}",
                                event.orderId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}