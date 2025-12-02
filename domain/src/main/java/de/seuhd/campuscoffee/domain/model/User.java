package de.seuhd.campuscoffee.domain.model;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Builder(toBuilder = true)
public record User (
        @Nullable Long id,
        @Nullable LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt,
        @NonNull String loginName,
        @NonNull String emailAddress,
        @NonNull String firstName,
        @NonNull String lastName
) implements Serializable { // serializable to allow cloning (see TestFixtures class).
    @Serial
    private static final long serialVersionUID = 1L;

    public User {
        Objects.requireNonNull(loginName, "loginName must not be null");
        Objects.requireNonNull(emailAddress, "emailAddress must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
    }
}
