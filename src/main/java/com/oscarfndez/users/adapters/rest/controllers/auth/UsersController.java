package com.oscarfndez.users.adapters.rest.controllers.auth;

import com.oscarfndez.framework.core.model.auth.User;
import com.oscarfndez.framework.core.model.dto.PageResponseDto;
import com.oscarfndez.users.adapters.rest.dtos.UserDto;
import com.oscarfndez.users.adapters.rest.dtos.mappers.UserModelDtoMapper;
import com.oscarfndez.users.core.services.auth.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/users", "/gamesCollection/api/users"})
@AllArgsConstructor
@PreAuthorize("@authorizationService.hasRole('ADMIN')")
public class UsersController {

    private final UserService userService;
    private final UserModelDtoMapper userModelDtoMapper;

    @GetMapping
    public ResponseEntity<UserDto> loadUser(@RequestParam UUID id) {
        return ResponseEntity.ok(userModelDtoMapper.mapToDto(userService.retrieveOne(id)));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponseDto<UserDto>> loadAllUsers(
            @RequestParam(required = false) final String search,
            @RequestParam(required = false, defaultValue = "firstName") final String sortField,
            @RequestParam(required = false, defaultValue = "asc") final String sortDir,
            @RequestParam(required = false, defaultValue = "0") final int page,
            @RequestParam(required = false, defaultValue = "10") final int size
    ) {
        Page<User> resultPage = userService.retrievePage(search, sortField, sortDir, page, size);
        List<UserDto> content = resultPage.getContent()
                .stream()
                .map(userModelDtoMapper::mapToDto)
                .toList();

        return ResponseEntity.ok(new PageResponseDto<>(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        ));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUser(@RequestParam UUID id, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userModelDtoMapper.mapToDto(userService.update(
                id,
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getEmail(),
                userDto.getRole()
        )));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateUserWithPhoto(@RequestParam UUID id,
            @RequestPart("user") UserDto userDto,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        return ResponseEntity.ok(userModelDtoMapper.mapToDto(userService.update(
                id,
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getEmail(),
                userDto.getRole(),
                photo
        )));
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> loadUserPhoto(@RequestParam UUID id) {
        User user = userService.retrieveOne(id);
        if (user.getPhoto() == null || user.getPhoto().length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, user.getPhotoContentType() != null
                        ? user.getPhotoContentType()
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(user.getPhoto());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> createUser(@RequestPart("user") UserDto userDto,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws IOException {
        return ResponseEntity.ok(userModelDtoMapper.mapToDto(userService.create(
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getEmail(),
                userDto.getPassword(),
                userDto.getRole(),
                photo
        )));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam UUID id) {
        userService.deleteOne(id);
        return ResponseEntity.noContent().build();
    }
}
