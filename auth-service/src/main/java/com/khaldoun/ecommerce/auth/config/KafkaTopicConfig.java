package com.khaldoun.ecommerce.auth.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String USER_REGISTERED_TOPIC = "user-registered";

    // Compacted: Kafka keeps only the newest message per key (userId), forever.
    // Like a phone book, so any service can rebuild its address book from page 1.
    @Bean
    NewTopic userRegisteredTopic() {
        return TopicBuilder.name(USER_REGISTERED_TOPIC)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }
}
