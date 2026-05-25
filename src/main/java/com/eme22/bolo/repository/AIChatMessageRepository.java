package com.eme22.bolo.repository;

import com.eme22.bolo.model.AIChatMessage;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AIChatMessageRepository implements PanacheRepository<AIChatMessage> {

    @Transactional
    public List<AIChatMessage> findActiveSessionMessages(Long guildId, Long channelId, Long userId, String sessionId) {
        return list("guildId = ?1 and channelId = ?2 and userId = ?3 and sessionId = ?4 order by timestamp asc", guildId, channelId, userId, sessionId);
    }

    @Transactional
    public void deleteSession(String sessionId) {
        delete("sessionId = ?1", sessionId);
    }
}
