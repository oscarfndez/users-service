package com.oscarfndez.users.core.events;

import com.oscarfndez.users.ports.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLifecycleReplayService {

    private final UserRepository userRepository;
    private final UserLifecycleEventPublisher userLifecycleEventPublisher;

    @Transactional(readOnly = true)
    public int replayExistingUsers() {
        var users = userRepository.findAll();
        users.forEach(userLifecycleEventPublisher::afterUpdating);
        log.info("Replayed {} user lifecycle events.", users.size());
        return users.size();
    }
}
