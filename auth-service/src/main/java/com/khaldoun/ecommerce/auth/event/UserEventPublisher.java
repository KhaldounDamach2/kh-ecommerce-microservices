package com.khaldoun.ecommerce.auth.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.khaldoun.ecommerce.auth.config.KafkaTopicConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Only after the confirmation is committed: never announce a user
    // whose confirmation was rolled back.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        // Key = userId: compaction keeps the newest entry per user.
        kafkaTemplate.send(KafkaTopicConfig.USER_REGISTERED_TOPIC, String.valueOf(event.userId()), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish user-registered for user {}", event.userId(), error);
                    } else {
                        log.info("Published user-registered for user {}", event.userId());
                    }
                });
    }
}
