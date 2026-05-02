package com.oscarfndez.users.core.events;

import java.time.Instant;
import java.util.UUID;

public record UserLifecycleEvent(
        String eventType,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant occurredAt
) {
}
