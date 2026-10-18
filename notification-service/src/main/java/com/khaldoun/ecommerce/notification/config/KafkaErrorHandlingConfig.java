package com.khaldoun.ecommerce.notification.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaErrorHandlingConfig {

    // Spring Boot plugs this bean into every @KafkaListener automatically.
    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate,
            ProducerFactory<String, Object> producerFactory) {

        // A poison pill (broken JSON) arrives as raw bytes, so it needs a
        // template that sends bytes unchanged. Everything else is an object
        // and goes through the normal JSON template. Order matters: byte[] first.
        KafkaTemplate<String, Object> bytesTemplate = new KafkaTemplate<>(producerFactory,
                Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class));
        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesTemplate);
        templates.put(Object.class, kafkaTemplate);

        // the "returned mail" clerk.
        // Create a DeadLetterPublishingRecoverer from `templates` and a lambda
        // (record, exception) -> new TopicPartition(<topic name + "-dlt">, <same
        // partition>)
        // Hint: record.topic() and record.partition()
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(templates,
                (record, exception) -> new TopicPartition(record.topic() + "-dlt", record.partition()));

        // the retry plan: 3 retries, pauses 1 s, 2 s, 4 s.
        // new ExponentialBackOffWithMaxRetries(3), then set:
        // initial interval 1_000L, multiplier 2.0, max interval 10_000L
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000L);

        // combine both.
        // return new DefaultErrorHandler(recoverer, backOff);
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
