package com.khaldoun.ecommerce.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDER_CREATED_TOPIC = "order-created";
    public static final String STOCK_RESULT_TOPIC = "stock-result";

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

}