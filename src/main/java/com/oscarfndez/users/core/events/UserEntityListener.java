package com.oscarfndez.users.core.events;

import com.oscarfndez.framework.core.model.auth.User;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserEntityListener {

    private static UserLifecycleEventPublisher publisher;

    public UserEntityListener() {
    }

    @Autowired
    public void setPublisher(UserLifecycleEventPublisher publisher) {
        UserEntityListener.publisher = publisher;
    }

    @PostPersist
    public void afterCreating(User user) {
        getPublisher().afterCreating(user);
    }

    @PostUpdate
    public void afterUpdating(User user) {
        getPublisher().afterUpdating(user);
    }

    @PostRemove
    public void afterDeleting(User user) {
        getPublisher().afterDeleting(user);
    }

    private UserLifecycleEventPublisher getPublisher() {
        if (publisher == null) {
            throw new IllegalStateException("UserLifecycleEventPublisher is not initialized.");
        }

        return publisher;
    }
}
