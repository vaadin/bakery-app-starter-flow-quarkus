package com.vaadin.starter.bakery.backend.service;

import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.repositories.UserRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class UserService implements FilterableCrudService<User> {

    public static final String MODIFY_LOCKED_USER_NOT_PERMITTED = "User has been locked and cannot be modified or deleted";
    private static final String DELETING_SELF_NOT_PERMITTED = "You cannot delete your own account";
    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAnyMatching(Optional<String> filter, Page page) {
        if (filter.isPresent()) {
            String repositoryFilter = "%" + filter.get() + "%";
            return getRepository()
                    .findByEmailLikeIgnoreCaseOrFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrRoleLikeIgnoreCase(
                            repositoryFilter, repositoryFilter, repositoryFilter, repositoryFilter, page);
        } else {
            return find(page);
        }
    }

    @Override
    public long countAnyMatching(Optional<String> filter) {
        if (filter.isPresent()) {
            String repositoryFilter = "%" + filter.get() + "%";
            return userRepository.countByEmailLikeIgnoreCaseOrFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrRoleLikeIgnoreCase(
                    repositoryFilter, repositoryFilter, repositoryFilter, repositoryFilter);
        } else {
            return count();
        }
    }

    @Override
    public UserRepository getRepository() {
        return userRepository;
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<User> find(Page page) {
        return getRepository().findBy(page);
    }

    @Override
    @Transactional
    public User save(User currentUser, User entity) {
        throwIfUserLocked(entity);
        return FilterableCrudService.super.save(currentUser, entity);
    }

    @Override
    @Transactional
    public void delete(User currentUser, User userToDelete) {
        throwIfDeletingSelf(currentUser, userToDelete);
        throwIfUserLocked(userToDelete);
        FilterableCrudService.super.delete(currentUser, userToDelete);
    }

    private void throwIfDeletingSelf(User currentUser, User user) {
        if (currentUser.equals(user)) {
            throw new UserFriendlyDataException(DELETING_SELF_NOT_PERMITTED);
        }
    }

    private void throwIfUserLocked(User entity) {
        if (entity != null && entity.isLocked()) {
            throw new UserFriendlyDataException(MODIFY_LOCKED_USER_NOT_PERMITTED);
        }
    }

    @Override
    public User createNew(User currentUser) {
        return new User();
    }
}
