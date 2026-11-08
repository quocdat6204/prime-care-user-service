package com.example.user_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.topic.email:email-notifications}")
    private String emailTopic;

    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(emailTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
