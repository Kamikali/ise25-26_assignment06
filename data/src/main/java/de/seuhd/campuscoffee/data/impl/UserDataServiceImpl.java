package de.seuhd.campuscoffee.data.impl;

import de.seuhd.campuscoffee.data.mapper.UserEntityMapper;
import de.seuhd.campuscoffee.data.persistence.UserEntity;
import de.seuhd.campuscoffee.data.persistence.UserRepository;
import de.seuhd.campuscoffee.data.util.ConstraintViolationChecker;
import de.seuhd.campuscoffee.domain.exceptions.DuplicationException;
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException;
import de.seuhd.campuscoffee.domain.model.User;
import de.seuhd.campuscoffee.domain.ports.UserDataService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the user data service that the domain layer provides as a port.
 * This layer is responsible for data access and persistence.
 * Business logic should be in the service layer.
 */
@Service
@RequiredArgsConstructor
class UserDataServiceImpl implements UserDataService {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    public void clear() {
        userRepository.deleteAllInBatch();
        userRepository.flush();
        userRepository.resetSequence(); // ensure consistent IDs after clearing (for local testing)
    }

    @Override
    public @NonNull List<User> getAll() {
        return userRepository.findAll().stream()
                .map(userEntityMapper::fromEntity)
                .toList();
    }

    @Override
    public @NonNull User getById(@NonNull Long id) {
        return userRepository.findById(id)
                .map(userEntityMapper::fromEntity)
                .orElseThrow(() -> new NotFoundException(User.class, id));
    }

    @Override
    public @NonNull User getByLoginName(@NonNull String loginName) {
        return userRepository.findByLoginName(loginName)
                .map(userEntityMapper::fromEntity)
                .orElseThrow(() -> new NotFoundException(User.class, UserEntity.LOGIN_NAME_COLUMN, loginName));
    }

    @Override
    public @NonNull User upsert(@NonNull User user) {
        try {
            if (user.id() == null) {
                return userEntityMapper.fromEntity(
                        userRepository.saveAndFlush(userEntityMapper.toEntity(user))
                );
            }

            UserEntity userEntity = userRepository.findById(user.id())
                    .orElseThrow(() -> new NotFoundException(User.class, user.id()));

            userEntityMapper.updateEntity(user, userEntity);

            return userEntityMapper.fromEntity(userRepository.saveAndFlush(userEntity));
        } catch (DataIntegrityViolationException e) {
            if (ConstraintViolationChecker.isConstraintViolation(e, UserEntity.LOGIN_NAME_CONSTRAINT)) {
                throw new DuplicationException(User.class, UserEntity.LOGIN_NAME_COLUMN, user.loginName());
            } else if (ConstraintViolationChecker.isConstraintViolation(e, UserEntity.EMAIL_ADDRESS_CONSTRAINT)) {
                throw new DuplicationException(User.class, UserEntity.EMAIL_ADDRESS_COLUMN, user.emailAddress());
            }
            throw e;
        }
    }

    @Override
    public void delete(@NonNull Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(User.class, id);
        }
        userRepository.deleteById(id);
    }
}
