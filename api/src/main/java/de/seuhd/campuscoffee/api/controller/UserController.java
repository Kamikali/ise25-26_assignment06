package de.seuhd.campuscoffee.api.controller;

import de.seuhd.campuscoffee.api.dtos.UserDto;
import de.seuhd.campuscoffee.api.mapper.UserDtoMapper;
import de.seuhd.campuscoffee.domain.ports.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.seuhd.campuscoffee.api.util.ControllerUtils.getLocation;

@Controller
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    @GetMapping("")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(
                userService.getAll().stream()
                        .map(userDtoMapper::fromDomain)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                userDtoMapper.fromDomain(userService.getById(id))
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<UserDto> filter(@RequestParam("loginName") String loginName) {
        return ResponseEntity.ok(
                userDtoMapper.fromDomain(userService.getByLoginName(loginName))
        );
    }

    @PostMapping("")
    public ResponseEntity<UserDto> create(@RequestBody @Valid UserDto userDto) {
        UserDto created = upsert(userDto);
        return ResponseEntity
                .created(getLocation(created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody @Valid UserDto userDto) {
        if (!id.equals(userDto.id())) {
            throw new IllegalArgumentException("User ID in path and body do not match.");
        }
        return ResponseEntity.ok(upsert(userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserDto upsert(UserDto userDto) {
        return userDtoMapper.fromDomain(
                userService.upsert(
                        userDtoMapper.toDomain(userDto)
                )
        );
    }
}
