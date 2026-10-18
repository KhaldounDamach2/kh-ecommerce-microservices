package com.khaldoun.ecommerce.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDER_CREATED_TOPIC = "order-created";
    public static final String STOCK_RESULT_TOPIC = "stock-result";
    public static final String STOCK_LOW_TOPIC = "stock-low";
    public static final String STOCK_DEPLETED_TOPIC = "stock-depleted";
    public static final String ORDER_CREATED_DLT = "order-created-dlt";

    // Same definition as order-service: safe and idempotent, and it means the
    // consumer never starts against a missing topic.
    @Bean
    NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic stockResultTopic() {
        return TopicBuilder.name(STOCK_RESULT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

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

    // Dead-letter topic ("returned mail"): same partition count, because a
    // failed message is parked in the same partition number it came from.
    @Bean
    NewTopic orderCreatedDltTopic() {
        return TopicBuilder.name(ORDER_CREATED_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

}