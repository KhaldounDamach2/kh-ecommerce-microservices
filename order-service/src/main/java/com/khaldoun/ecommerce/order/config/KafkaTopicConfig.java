package com.khaldoun.ecommerce.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDER_CREATED_TOPIC = "order-created";
    public static final String ORDER_STATUS_CHANGED_TOPIC = "order-status-changed";
    public static final String STOCK_RESULT_TOPIC = "stock-result";
    public static final String STOCK_RESULT_DLT = "stock-result-dlt";

    @Bean
    NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic orderStatusChangedTopic() {
        return TopicBuilder.name(ORDER_STATUS_CHANGED_TOPIC)
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

    // Dead-letter topics ("returned mail"): same partition count as the
    // originals, because a failed message keeps its partition number.
    @Bean
    NewTopic stockResultDltTopic() {
        return TopicBuilder.name(STOCK_RESULT_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
