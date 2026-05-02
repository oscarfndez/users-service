package com.oscarfndez.users.core.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oscarfndez.framework.core.model.auth.Role;
import com.oscarfndez.framework.core.model.auth.User;
import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserLifecycleEventPublisherTest {

    private final JmsTemplate jmsTemplate = mock(JmsTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final UserLifecycleEventPublisher publisher = new UserLifecycleEventPublisher(jmsTemplate, objectMapper);

    @Test
    void afterCreatingPublishesUserLifecycleEventAsJson() throws Exception {
        ReflectionTestUtils.setField(publisher, "userEventsEnabled", true);
        ReflectionTestUtils.setField(publisher, "userEventsTopic", "test.user-events");
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .firstName("Oscar")
                .lastName("Fernandez")
                .email("oscar@example.com")
                .role(Role.ADMIN)
                .build();

        publisher.afterCreating(user);

        var topicCaptor = forClass(String.class);
        var payloadCaptor = forClass(Object.class);
        verify(jmsTemplate).convertAndSend(topicCaptor.capture(), payloadCaptor.capture());

        JsonNode payload = objectMapper.readTree((String) payloadCaptor.getValue());
        assertThat(topicCaptor.getValue()).isEqualTo("test.user-events");
        assertThat(payload.get("eventType").asText()).isEqualTo("afterCreating");
        assertThat(payload.get("userId").asText()).isEqualTo(userId.toString());
        assertThat(payload.get("email").asText()).isEqualTo("oscar@example.com");
        assertThat(payload.get("firstName").asText()).isEqualTo("Oscar");
        assertThat(payload.get("lastName").asText()).isEqualTo("Fernandez");
        assertThat(payload.get("role").asText()).isEqualTo("ADMIN");
        assertThat(payload.get("occurredAt").asText()).isNotBlank();
    }
}
