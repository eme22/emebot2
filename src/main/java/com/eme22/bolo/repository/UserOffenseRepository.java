package com.eme22.bolo.repository;

import com.eme22.bolo.model.UserOffense;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserOffenseRepository implements PanacheRepository<UserOffense> {

    public UserOffense findByUserId(Long userId) {
        return find("userId = ?1", userId).firstResult();
    }
}
