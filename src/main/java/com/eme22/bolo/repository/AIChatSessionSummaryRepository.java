package com.eme22.bolo.repository;

import com.eme22.bolo.model.AIChatSessionSummary;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AIChatSessionSummaryRepository implements PanacheRepository<AIChatSessionSummary> {

    @Transactional
    public AIChatSessionSummary findBySessionId(String sessionId) {
        return find("sessionId", sessionId).firstResult();
    }

    @Transactional
    public void deleteBySessionId(String sessionId) {
        delete("sessionId", sessionId);
    }
}
