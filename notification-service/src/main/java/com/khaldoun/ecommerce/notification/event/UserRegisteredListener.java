package com.khaldoun.ecommerce.notification.event;

import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.khaldoun.ecommerce.notification.config.KafkaTopicConfig;
import com.khaldoun.ecommerce.notification.domain.UserContact;
import com.khaldoun.ecommerce.notification.repository.UserContactRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredListener {

    private final UserContactRepository userContactRepository;

    // application.yaml says "every message is a StockLevelEvent".
    // This listener overrides that for itself only.

    @KafkaListener(topics = KafkaTopicConfig.USER_REGISTERED_TOPIC, properties = "spring.json.value.default.type=com.khaldoun.ecommerce.notification.event.UserRegisteredEvent")
    public void onUserRegistered(UserRegisteredEvent event) {
        // TODO (you):
        // 1. Never trust a letter blindly: if userId or email is null, log a warning
        // and return.
        // 2. Save (insert OR update) the contact:
        // userContactRepository.save(new UserContact(userId, email, role,
        // Instant.now()))
        // Saving the same user twice just overwrites: naturally idempotent.
        // 3. Log something like "Saved scontact for user {}".
        if (event.userId() == null || event.email() == null) {
            log.warn("Received UserRegisteredEvent with null userId or email: {}", event);
            return;
        }
        UserContact contact = new UserContact(
                event.userId(),
                event.email(),
                event.role(),
                Instant.now());
        userContactRepository.save(contact);
        log.info("Saved contact for user {}", event.userId());

    }
}
