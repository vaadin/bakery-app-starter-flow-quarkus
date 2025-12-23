package com.vaadin.starter.bakery.backend.repositories;

import com.vaadin.starter.bakery.backend.data.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public User findByEmailIgnoreCase(String email) {
        return find("where upper(email) = upper(?1)", email).singleResult();
    }

    public List<User> findBy(Page pageable) {
        return findAll().page(pageable).list();
    }

    public List<User> findByEmailLikeIgnoreCaseOrFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrRoleLikeIgnoreCase(
            String emailLike, String firstNameLike, String lastNameLike, String roleLike, Page pageable) {
        return find("where upper(email) like upper(?1) or upper(firstName) like upper(?2) or upper(lastName) like upper(?3) or upper(role) like upper(?4)",
                emailLike, firstNameLike, lastNameLike, roleLike).page(pageable).list();
    }

    public long countByEmailLikeIgnoreCaseOrFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrRoleLikeIgnoreCase(
            String emailLike, String firstNameLike, String lastNameLike, String roleLike) {
        return count("where upper(email) like upper(?1) or upper(firstName) like upper(?2) or upper(lastName) like upper(?3) or upper(role) like upper(?4)",
                emailLike, firstNameLike, lastNameLike, roleLike);
    }
}
