package com.oscarfndez.users.core.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscarfndez.framework.core.model.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLifecycleEventPublisher {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${user.events.enabled:true}")
    private boolean userEventsEnabled;

    @Value("${user.events.topic:games-collection.user-events}")
    private String userEventsTopic;

    public void afterCreating(User user) {
        publish("afterCreating", user);
    }

    public void afterUpdating(User user) {
        publish("afterUpdating", user);
    }

    public void afterDeleting(User user) {
        publish("afterDeleting", user);
    }

    private void publish(String eventType, User user) {
        if (!userEventsEnabled) {
            log.debug("User lifecycle event publishing disabled type={} userId={}", eventType, user.getId());
            return;
        }

        UserLifecycleEvent event = new UserLifecycleEvent(
                eventType,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                Instant.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            jmsTemplate.convertAndSend(userEventsTopic, payload);
            log.info("Published user lifecycle event type={} userId={} topic={}", eventType, user.getId(), userEventsTopic);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize user lifecycle event.", exception);
        }
    }
}
