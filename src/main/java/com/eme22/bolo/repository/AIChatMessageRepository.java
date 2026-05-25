package com.eme22.bolo.repository;

import com.eme22.bolo.model.AIChatMessage;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AIChatMessageRepository implements PanacheRepository<AIChatMessage> {

    @Transactional
    public List<AIChatMessage> findActiveSessionMessages(Long guildId, Long channelId, String sessionId) {
        return list("guildId = ?1 and channelId = ?2 and sessionId = ?3 order by timestamp asc", guildId, channelId, sessionId);
    }

    @Transactional
    public AIChatMessage findByDiscordMessageId(Long discordMessageId) {
        return find("discordMessageId = ?1", discordMessageId).firstResult();
    }

    @Transactional
    public void deleteSession(String sessionId) {
        delete("sessionId = ?1", sessionId);
    }
}
