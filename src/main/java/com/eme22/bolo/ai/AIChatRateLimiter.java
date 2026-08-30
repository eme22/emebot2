package com.eme22.bolo.ai;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Slf4j
public class AIChatRateLimiter {

    @ConfigProperty(name = "openai.user-cooldown-seconds", defaultValue = "3")
    int userCooldownSeconds;

    @ConfigProperty(name = "openai.max-concurrent-per-channel", defaultValue = "1")
    int maxConcurrentPerChannel;

    private final ConcurrentHashMap<Long, Instant> lastRequestPerUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Integer> inFlightPerChannel = new ConcurrentHashMap<>();

    /**
     * @return true si el usuario debe ser ignorado porque envió mensajes demasiado rápido.
     */
    public boolean isUserInCooldown(long userId) {
        if (userCooldownSeconds <= 0) return false;
        Instant now = Instant.now();
        Instant last = lastRequestPerUser.get(userId);
        if (last != null && Duration.between(last, now).getSeconds() < userCooldownSeconds) {
            return true;
        }
        lastRequestPerUser.put(userId, now);
        return false;
    }

    /**
     * Intenta reservar un slot de procesamiento para el canal.
     * @return true si se obtuvo el slot (llamar a {@link #releaseChannel} al terminar), false si el canal superó su límite.
     */
    public boolean tryAcquireChannel(long channelId) {
        int limit = Math.max(1, maxConcurrentPerChannel);
        boolean[] acquired = {false};
        inFlightPerChannel.compute(channelId, (k, v) -> {
            int current = v == null ? 0 : v;
            if (current < limit) {
                acquired[0] = true;
                return current + 1;
            }
            return v;
        });
        return acquired[0];
    }

    public void releaseChannel(long channelId) {
        inFlightPerChannel.computeIfPresent(channelId, (k, v) -> v <= 1 ? null : v - 1);
    }
}
