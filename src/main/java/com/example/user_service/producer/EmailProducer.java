package com.example.user_service.producer;

import com.example.user_service.dto.event.EmailEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailProducer {

    KafkaTemplate<String, Object> kafkaTemplate;

    @NonFinal
    @Value("${kafka.topic.email:email-notifications}")
    String emailTopic;

    public void sendEmailEvent(EmailEvent event) {
        log.info("Sending email event to Kafka topic [{}]: recipient={}", emailTopic, event.getTo());
        try {
            kafkaTemplate.send(emailTopic, event.getTo(), event);
        } catch (Exception e) {
            log.error("Failed to send email event to Kafka: ", e);
        }
    }
}
