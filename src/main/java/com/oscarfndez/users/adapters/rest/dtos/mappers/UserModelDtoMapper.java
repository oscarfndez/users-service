package com.oscarfndez.users.adapters.rest.dtos.mappers;

import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.users.adapters.rest.dtos.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserModelDtoMapper {

    public UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .hasPhoto(user.getPhoto() != null && user.getPhoto().length > 0)
                .build();
    }
}
