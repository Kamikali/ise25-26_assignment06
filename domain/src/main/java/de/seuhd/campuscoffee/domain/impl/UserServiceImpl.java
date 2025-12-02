package de.seuhd.campuscoffee.domain.impl;

import de.seuhd.campuscoffee.domain.model.User;
import de.seuhd.campuscoffee.domain.ports.UserDataService;
import de.seuhd.campuscoffee.domain.ports.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDataService userDataService;

    @Override
    public void clear() {
        log.warn("Clearing all Users");
        userDataService.clear();
    }

    @Override
    public @NonNull List<User> getAll() {
        log.debug("Retrieving all users");
        return userDataService.getAll();
    }

    @Override
    public @NonNull User getById(@NonNull Long id) {
        log.debug("Retrieving user with ID: {}", id);
        return userDataService.getById(id);
    }

    @Override
    public @NonNull User getByLoginName(@NonNull String loginName) {
        log.debug("Retrieving user with loginName: {}", loginName);
        return userDataService.getByLoginName(loginName);
    }

    @Override
    public @NonNull User upsert(@NonNull User user) {
        if (user.id() == null) {
            log.info("Creating new user: {}", user.loginName());
        } else {
            log.info("Updating user with ID: {}", user.id());
            Objects.requireNonNull(user.id());
            userDataService.getById(user.id());
        }
        User upserted = userDataService.upsert(user);
        log.info("Successfully upserted user with ID: {}", upserted.id());
        return upserted;
    }

    @Override
    public void delete(@NonNull Long id) {
        log.info("Trying to delete user with ID: {}", id);
        userDataService.delete(id);
        log.info("Deleted user with ID: {}", id);
    }
}
