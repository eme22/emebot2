package com.eme22.bolo.ai;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AIChatSessionManager {

    @ConfigProperty(name = "openai.session-timeout-minutes", defaultValue = "15")
    int timeoutMinutes;

    private final ConcurrentHashMap<String, SessionState> sessions = new ConcurrentHashMap<>();

    public String getOrCreateSession(Long guildId, Long channelId, Long userId) {
        String key = guildId + "-" + channelId + "-" + userId;
        SessionState state = sessions.get(key);
        Instant now = Instant.now();

        if (state == null || now.isAfter(state.lastActivity.plusSeconds(timeoutMinutes * 60L))) {
            String newSessionId = UUID.randomUUID().toString();
            state = new SessionState(newSessionId, now);
            sessions.put(key, state);
        } else {
            state.lastActivity = now;
        }
        return state.sessionId;
    }

    public void forceReset(Long guildId, Long channelId, Long userId) {
        String key = guildId + "-" + channelId + "-" + userId;
        sessions.remove(key);
    }

    private static class SessionState {
        String sessionId;
        Instant lastActivity;

        SessionState(String sessionId, Instant lastActivity) {
            this.sessionId = sessionId;
            this.lastActivity = lastActivity;
        }
    }
}
