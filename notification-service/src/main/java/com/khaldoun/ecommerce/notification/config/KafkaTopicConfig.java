package com.khaldoun.ecommerce.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String STOCK_LOW_TOPIC = "stock-low";
    public static final String STOCK_DEPLETED_TOPIC = "stock-depleted";

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
}
