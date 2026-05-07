package com.eme22.bolo.repository;

import com.eme22.bolo.model.Server;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerRepository implements PanacheRepository<Server> {
}
