package com.khaldoun.ecommerce.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String STOCK_LOW_TOPIC = "stock-low";
    public static final String STOCK_DEPLETED_TOPIC = "stock-depleted";
    public static final String USER_REGISTERED_TOPIC = "user-registered";
    public static final String STOCK_LOW_DLT = "stock-low-dlt";
    public static final String STOCK_DEPLETED_DLT = "stock-depleted-dlt";
    public static final String USER_REGISTERED_DLT = "user-registered-dlt";

    // Same definitions as product-service: safe and idempotent, and it means
    // the listener never starts against a missing topic.
    @Bean
    NewTopic stockLowTopic() {
        return TopicBuilder.name(STOCK_LOW_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic stockDepletedTopic() {
        return TopicBuilder.name(STOCK_DEPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Same definition as auth-service (compacted phone book).
    @Bean
    NewTopic userRegisteredTopic() {
        return TopicBuilder.name(USER_REGISTERED_TOPIC)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    // Dead-letter topics ("returned mail"): same partition count as the
    // originals, because a failed message keeps its partition number.
    @Bean
    NewTopic stockLowDltTopic() {
        return TopicBuilder.name(STOCK_LOW_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic stockDepletedDltTopic() {
        return TopicBuilder.name(STOCK_DEPLETED_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic userRegisteredDltTopic() {
        return TopicBuilder.name(USER_REGISTERED_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
